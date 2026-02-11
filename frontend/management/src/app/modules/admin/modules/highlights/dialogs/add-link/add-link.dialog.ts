import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { BehaviorSubject, combineLatest, finalize, first, map, Observable, Subject } from 'rxjs';
import { Option, someOrNone, validateProps } from '@kicherkrabbe/shared';
import { NotificationService } from '../../../../../shared';
import { Link, LinkType } from '../../model';
import { HighlightsService } from '../../services';

interface LinkOption {
  type: LinkType;
  id: string;
  name: string;
}

export class AddLinkDialogData {
  readonly highlight: Option<{ id: string; version: number }>;
  readonly existingLinks: Link[];

  private constructor(props: { highlight: Option<{ id: string; version: number }>; existingLinks: Link[] }) {
    validateProps(props);

    this.highlight = props.highlight;
    this.existingLinks = props.existingLinks;
  }

  static of(props: { highlight?: { id: string; version: number }; existingLinks?: Link[] }): AddLinkDialogData {
    return new AddLinkDialogData({
      highlight: someOrNone(props.highlight),
      existingLinks: someOrNone(props.existingLinks).orElse([]),
    });
  }
}

export interface AddLinkDialogResult {
  version: number;
  link: Link;
}

@Component({
  selector: 'app-add-link-dialog',
  templateUrl: './add-link.dialog.html',
  styleUrls: ['./add-link.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class AddLinkDialog implements OnInit, OnDestroy {
  protected readonly searchTerm$ = new BehaviorSubject<string>('');
  protected readonly addingLink$ = new BehaviorSubject<boolean>(false);
  protected readonly loadingLinks$ = new BehaviorSubject<boolean>(false);
  protected readonly linksLoaded$ = new BehaviorSubject<boolean>(false);

  private readonly allLinks$ = new BehaviorSubject<LinkOption[]>([]);

  protected readonly filteredLinks$: Observable<LinkOption[]> = combineLatest([this.allLinks$, this.searchTerm$]).pipe(
    map(([links, searchTerm]) => {
      if (!searchTerm.trim()) {
        return links;
      }
      const term = searchTerm.toLowerCase();
      return links.filter((link) => link.name.toLowerCase().includes(term));
    }),
  );

  protected readonly loading$ = combineLatest([this.loadingLinks$, this.addingLink$]).pipe(
    map(([loadingLinks, addingLink]) => loadingLinks || addingLink),
  );

  protected readonly alreadyAddedLinkIdentifiers = new Set<string>();
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly data: AddLinkDialogData,
    private readonly dialog: Dialog<AddLinkDialogResult>,
    private readonly dialogService: DialogService,
    private readonly highlightsService: HighlightsService,
    private readonly notificationService: NotificationService,
  ) {
    this.data.existingLinks.forEach((link) => this.alreadyAddedLinkIdentifiers.add(`${link.type}-${link.id}`));
  }

  ngOnInit(): void {
    this.loadAllLinks();
  }

  ngOnDestroy(): void {
    this.searchTerm$.complete();
    this.allLinks$.complete();
    this.loadingLinks$.complete();
    this.linksLoaded$.complete();
    this.addingLink$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  isAlreadyAdded(link: LinkOption): boolean {
    return this.alreadyAddedLinkIdentifiers.has(`${link.type}-${link.id}`);
  }

  close(): void {
    this.dialogService.close(this.dialog.id);
  }

  addLink(linkOption: LinkOption): void {
    if (this.isAlreadyAdded(linkOption)) {
      return;
    }

    if (this.addingLink$.value) {
      return;
    }
    this.addingLink$.next(true);

    const link = Link.of({ type: linkOption.type, id: linkOption.id, name: linkOption.name });

    this.data.highlight.ifSomeOrElse(
      (highlight) =>
        this.highlightsService
          .addLink(highlight.id, highlight.version, linkOption.type, linkOption.id, linkOption.name)
          .pipe(
            first(),
            finalize(() => this.addingLink$.next(false)),
          )
          .subscribe({
            next: (version) => {
              this.notificationService.publish({
                message: 'Link wurde hinzugefügt.',
                type: 'success',
              });
              this.dialog.attachResult({ version, link });
              this.close();
            },
            error: (e) => {
              console.error('Failed to add link', e);
              this.notificationService.publish({
                message: 'Link konnte nicht hinzugefügt werden. Bitte versuche es erneut.',
                type: 'error',
              });
            },
          }),
      () => {
        this.dialog.attachResult({ version: 0, link });
        this.close();
      },
    );
  }

  updateSearchTerm(value: string): void {
    this.searchTerm$.next(value);
  }

  getLinkTypeLabel(type: LinkType): string {
    return type === LinkType.PATTERN ? 'Schnittmuster' : 'Stoff';
  }

  private loadAllLinks(): void {
    this.loadingLinks$.next(true);

    this.highlightsService
      .getLinks({})
      .pipe(
        first(),
        finalize(() => {
          this.loadingLinks$.next(false);
          this.linksLoaded$.next(true);
        }),
      )
      .subscribe({
        next: (links) => {
          const linkOptions: LinkOption[] = links.map((link) => ({
            type: link.type,
            id: link.id,
            name: link.name,
          }));
          this.allLinks$.next(linkOptions.sort((a, b) => a.name.localeCompare(b.name)));
        },
        error: (e) => {
          console.error('Failed to load links', e);
          this.notificationService.publish({
            message: 'Links konnten nicht geladen werden. Bitte versuche es erneut.',
            type: 'error',
          });
        },
      });
  }
}
