import { ChangeDetectionStrategy, Component, Injector, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, catchError, finalize, first, map, of, Subject, takeUntil } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { none, Option, some } from '@kicherkrabbe/shared';
import { ButtonSize, NotificationService } from '../../../../../shared';
import { Highlight, Link } from '../../model';
import { HighlightsService } from '../../services';
import { environment } from '../../../../../../../environments';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { AddLinkDialog, AddLinkDialogData, AddLinkDialogResult } from '../../dialogs';

@Component({
  selector: 'app-highlight-page',
  templateUrl: './highlight.page.html',
  styleUrls: ['./highlight.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class HighlightPage implements OnInit, OnDestroy {
  protected readonly highlight$ = new BehaviorSubject<Option<Highlight>>(none());
  private readonly loadingHighlight$ = new BehaviorSubject<boolean>(false);
  protected readonly highlightLoaded$ = new BehaviorSubject<boolean>(false);

  protected readonly deleting$ = new BehaviorSubject<boolean>(false);
  protected readonly publishing$ = new BehaviorSubject<boolean>(false);
  protected readonly updatingImage$ = new BehaviorSubject<boolean>(false);
  protected readonly updatingSortOrder$ = new BehaviorSubject<boolean>(false);
  protected readonly removingLinkId$ = new BehaviorSubject<string | null>(null);

  protected readonly editingImage$ = new BehaviorSubject<boolean>(false);
  protected readonly editingSortOrder$ = new BehaviorSubject<boolean>(false);

  protected readonly sortOrderValue$ = new BehaviorSubject<number>(0);

  protected readonly loading$ = this.loadingHighlight$.asObservable();

  protected readonly ButtonSize = ButtonSize;

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly highlightsService: HighlightsService,
    private readonly dialogService: DialogService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.route.params
      .pipe(
        map((params) => params['highlightId']),
        takeUntil(this.destroy$),
      )
      .subscribe((highlightId) => this.loadHighlight(highlightId));
  }

  ngOnDestroy(): void {
    this.highlight$.complete();
    this.loadingHighlight$.complete();
    this.highlightLoaded$.complete();
    this.deleting$.complete();
    this.publishing$.complete();
    this.updatingImage$.complete();
    this.updatingSortOrder$.complete();
    this.removingLinkId$.complete();
    this.editingImage$.complete();
    this.editingSortOrder$.complete();
    this.sortOrderValue$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  togglePublish(highlight: Highlight): void {
    this.publishing$.next(true);

    const action$ = highlight.published
      ? this.highlightsService.unpublishHighlight(highlight.id, highlight.version)
      : this.highlightsService.publishHighlight(highlight.id, highlight.version);

    action$
      .pipe(
        first(),
        finalize(() => this.publishing$.next(false)),
      )
      .subscribe({
        next: (version) => {
          const message = highlight.published
            ? 'Das Highlight wurde zurückgezogen.'
            : 'Das Highlight wurde veröffentlicht.';
          this.notificationService.publish({ type: 'success', message });
          this.highlight$.next(some(highlight.togglePublished(version)));
        },
        error: (e) => {
          console.error(e);
          if (this.isVersionConflict(e)) {
            this.notificationService.publish({
              type: 'error',
              message: 'Das Highlight wurde zwischenzeitlich geändert. Die Seite wird neu geladen.',
            });
            this.reloadHighlight();
          } else {
            this.notificationService.publish({
              type: 'error',
              message: 'Die Aktion konnte nicht durchgeführt werden.',
            });
          }
        },
      });
  }

  delete(highlight: Highlight): void {
    if (!confirm('Möchtest du dieses Highlight wirklich löschen?')) {
      return;
    }

    this.deleting$.next(true);
    this.highlightsService
      .deleteHighlight(highlight.id, highlight.version)
      .pipe(
        first(),
        finalize(() => this.deleting$.next(false)),
      )
      .subscribe({
        next: () => {
          this.notificationService.publish({
            type: 'success',
            message: 'Das Highlight wurde gelöscht.',
          });
          this.router.navigate(['..'], { relativeTo: this.route });
        },
        error: (e) => {
          console.error(e);
          if (this.isVersionConflict(e)) {
            this.notificationService.publish({
              type: 'error',
              message: 'Das Highlight wurde zwischenzeitlich geändert. Die Seite wird neu geladen.',
            });
            this.reloadHighlight();
          } else {
            this.notificationService.publish({
              type: 'error',
              message: 'Das Highlight konnte nicht gelöscht werden.',
            });
          }
        },
      });
  }

  startEditingImage(): void {
    this.editingImage$.next(true);
  }

  cancelEditingImage(): void {
    this.editingImage$.next(false);
  }

  onImageUploaded(highlight: Highlight, newImageIds: string[]): void {
    if (newImageIds.length === 0) {
      return;
    }

    const newImageId = newImageIds[0];
    this.updatingImage$.next(true);
    this.highlightsService
      .updateImage(highlight.id, highlight.version, newImageId)
      .pipe(
        first(),
        finalize(() => {
          this.updatingImage$.next(false);
          this.editingImage$.next(false);
        }),
      )
      .subscribe({
        next: (version) => {
          this.notificationService.publish({
            type: 'success',
            message: 'Das Bild wurde aktualisiert.',
          });
          this.highlight$.next(some(highlight.updateImage(version, newImageId)));
        },
        error: (e) => {
          console.error(e);
          if (this.isVersionConflict(e)) {
            this.notificationService.publish({
              type: 'error',
              message: 'Das Highlight wurde zwischenzeitlich geändert. Die Seite wird neu geladen.',
            });
            this.reloadHighlight();
          } else {
            this.notificationService.publish({
              type: 'error',
              message: 'Das Bild konnte nicht aktualisiert werden.',
            });
          }
        },
      });
  }

  startEditingSortOrder(highlight: Highlight): void {
    this.sortOrderValue$.next(highlight.sortOrder);
    this.editingSortOrder$.next(true);
  }

  cancelEditingSortOrder(): void {
    this.editingSortOrder$.next(false);
  }

  updateSortOrderValue(value: string): void {
    const parsed = parseInt(value, 10);
    this.sortOrderValue$.next(isNaN(parsed) ? 0 : parsed);
  }

  saveSortOrder(highlight: Highlight): void {
    const newSortOrder = this.sortOrderValue$.value;
    if (newSortOrder === highlight.sortOrder) {
      this.editingSortOrder$.next(false);
      return;
    }

    this.updatingSortOrder$.next(true);
    this.highlightsService
      .updateSortOrder(highlight.id, highlight.version, newSortOrder)
      .pipe(
        first(),
        finalize(() => {
          this.updatingSortOrder$.next(false);
          this.editingSortOrder$.next(false);
        }),
      )
      .subscribe({
        next: (version) => {
          this.notificationService.publish({
            type: 'success',
            message: 'Die Sortierung wurde aktualisiert.',
          });
          this.highlight$.next(some(highlight.updateSortOrder(version, newSortOrder)));
        },
        error: (e) => {
          console.error(e);
          if (this.isVersionConflict(e)) {
            this.notificationService.publish({
              type: 'error',
              message: 'Das Highlight wurde zwischenzeitlich geändert. Die Seite wird neu geladen.',
            });
            this.reloadHighlight();
          } else {
            this.notificationService.publish({
              type: 'error',
              message: 'Die Sortierung konnte nicht aktualisiert werden.',
            });
          }
        },
      });
  }

  addLink(highlight: Highlight): void {
    const dialog = Dialog.create<AddLinkDialogResult>({
      title: 'Link hinzufügen',
      componentType: AddLinkDialog,
      injector: Injector.create({
        providers: [
          {
            provide: AddLinkDialogData,
            useValue: AddLinkDialogData.of({
              highlight: { id: highlight.id, version: highlight.version },
              existingLinks: highlight.links,
            }),
          },
          {
            provide: HighlightsService,
            useValue: this.highlightsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog
        .getResult()
        .map((result) => highlight.addLink(result.version, result.link))
        .ifSome((updatedHighlight) => this.highlight$.next(some(updatedHighlight)));
    });
  }

  removeLink(event: Event, highlight: Highlight, link: Link): void {
    event.stopPropagation();
    event.preventDefault();

    this.removingLinkId$.next(link.id);
    this.highlightsService
      .removeLink(highlight.id, highlight.version, link.type, link.id)
      .pipe(
        first(),
        finalize(() => this.removingLinkId$.next(null)),
      )
      .subscribe({
        next: (version) => {
          this.notificationService.publish({
            type: 'success',
            message: 'Der Link wurde entfernt.',
          });
          this.highlight$.next(some(highlight.removeLink(version, link.type, link.id)));
        },
        error: (e) => {
          console.error(e);
          if (this.isVersionConflict(e)) {
            this.notificationService.publish({
              type: 'error',
              message: 'Das Highlight wurde zwischenzeitlich geändert. Die Seite wird neu geladen.',
            });
            this.reloadHighlight();
          } else {
            this.notificationService.publish({
              type: 'error',
              message: 'Der Link konnte nicht entfernt werden.',
            });
          }
        },
      });
  }

  private isVersionConflict(error: unknown): boolean {
    return error instanceof HttpErrorResponse && error.status === 409;
  }

  private reloadHighlight(): void {
    this.highlight$.value.ifSome((highlight) => this.loadHighlight(highlight.id));
  }

  private loadHighlight(highlightId: string): void {
    if (this.loadingHighlight$.value) {
      return;
    }
    this.loadingHighlight$.next(true);

    this.highlightsService
      .getHighlight(highlightId)
      .pipe(
        first(),
        map((highlight) => some(highlight)),
        catchError((e) => {
          console.error('Failed to load highlight', e);
          this.notificationService.publish({
            type: 'error',
            message: 'Das Highlight konnte nicht geladen werden.',
          });
          return of(none<Highlight>());
        }),
        finalize(() => {
          this.loadingHighlight$.next(false);
          this.highlightLoaded$.next(true);
        }),
      )
      .subscribe((highlight) => this.highlight$.next(highlight));
  }
}
