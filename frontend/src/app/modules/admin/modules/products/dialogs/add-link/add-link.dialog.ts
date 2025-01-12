import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { ProductsService } from '../../services';
import {
  BehaviorSubject,
  combineLatest,
  debounceTime,
  finalize,
  first,
  map,
  Observable,
  Subject,
  takeUntil,
} from 'rxjs';
import { Link, Product } from '../../model';
import { someOrNone } from '../../../../../shared/modules/option';
import { NotificationService } from '../../../../../shared';

const LINKS_LIMIT = 20;

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

  protected readonly links$ = new BehaviorSubject<Link[]>([]);
  protected readonly totalLinks$ = new BehaviorSubject<number>(0);
  protected readonly loadingLinks$ = new BehaviorSubject<boolean>(false);
  protected readonly linksLoaded$ = new BehaviorSubject<boolean>(false);

  protected readonly loading$ = combineLatest([this.loadingLinks$, this.addingLink$]).pipe(
    map(([loadingLinks, addingLink]) => loadingLinks || addingLink),
  );
  protected readonly remainingLinksCount$: Observable<number> = combineLatest([this.links$, this.totalLinks$]).pipe(
    map(([links, totalLinks]) => totalLinks - links.length),
  );
  protected readonly moreLinksAvailable$: Observable<boolean> = this.remainingLinksCount$.pipe(
    map((remainingLinksCount) => remainingLinksCount > 0),
  );

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly product: Product,
    private readonly dialog: Dialog<AddLinkDialogResult>,
    private readonly dialogService: DialogService,
    private readonly productsService: ProductsService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.searchTerm$.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe((searchTerm) =>
      this.reloadLinks({
        searchTerm,
      }),
    );
  }

  ngOnDestroy(): void {
    this.searchTerm$.complete();
    this.links$.complete();
    this.totalLinks$.complete();
    this.loadingLinks$.complete();
    this.linksLoaded$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  close(): void {
    this.dialogService.close(this.dialog.id);
  }

  addLink(link: Link): void {
    if (this.addingLink$.value) {
      return;
    }
    this.addingLink$.next(true);

    this.productsService
      .addLink({
        id: this.product.id,
        version: this.product.version,
        linkType: link.type,
        linkId: link.id,
      })
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
          this.dialog.attachResult({
            version,
            link,
          });
          this.close();
        },
        error: (e) => {
          console.error('Failed to add link', e);
          this.notificationService.publish({
            message: 'Link konnte nicht hinzugefügt werden. Bitte versuche es erneut.',
            type: 'error',
          });
        },
      });
  }

  updateSearchTerm(value: string): void {
    this.searchTerm$.next(value);
  }

  loadMoreLinks(): void {
    const skip = this.links$.value.length;
    const limit = LINKS_LIMIT;

    this.reloadLinks({
      searchTerm: this.searchTerm$.value,
      skip,
      limit,
      keepLoadedLinks: true,
    });
  }

  private reloadLinks(props: { searchTerm?: string; skip?: number; limit?: number; keepLoadedLinks?: boolean }): void {
    if (this.loadingLinks$.value) {
      return;
    }
    this.loadingLinks$.next(true);

    const searchTerm = someOrNone(props.searchTerm).orElse('');
    const skip = someOrNone(props.skip).orElse(0);
    const limit = someOrNone(props.limit).orElse(LINKS_LIMIT);
    const keepLoadedLinks = someOrNone(props.keepLoadedLinks).orElse(false);

    this.productsService
      .getLinks({
        searchTerm,
        skip,
        limit,
      })
      .pipe(
        first(),
        finalize(() => {
          this.loadingLinks$.next(false);
          this.linksLoaded$.next(true);
        }),
      )
      .subscribe({
        next: (result) => {
          this.totalLinks$.next(result.total);

          if (keepLoadedLinks) {
            this.links$.next([...this.links$.value, ...result.links]);
          } else {
            this.links$.next(result.links);
          }
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
