import { ChangeDetectionStrategy, Component, EnvironmentInjector, OnDestroy } from '@angular/core';
import { HighlightsService } from '../../services';
import { BehaviorSubject, combineLatest, delay, finalize, map, Observable, Subject, takeUntil } from 'rxjs';
import { ButtonSize, NotificationService } from '../../../../../shared';
import { ActivatedRoute, Router } from '@angular/router';
import { none, Option, some } from '@kicherkrabbe/shared';
import { environment } from '../../../../../../../environments';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import {
  AssetSelectDialog,
  AssetSelectDialogData,
  AssetSelectDialogResult,
} from '../../../assets/dialogs';
import { AssetsService } from '../../../assets/services/assets.service';

@Component({
  selector: 'app-create-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class CreatePage implements OnDestroy {
  private readonly imageId$: BehaviorSubject<Option<string>> = new BehaviorSubject<Option<string>>(none());
  protected readonly hasImage$: Observable<boolean> = this.imageId$.pipe(map((id) => id.isSome()));
  protected readonly currentImageId$: Observable<string | null> = this.imageId$.pipe(map((id) => (id.isSome() ? id.orElseThrow() : null)));

  private readonly sortOrder$: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  private readonly sortOrderTouched$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly sortOrderValid$: Observable<boolean> = this.sortOrder$.pipe(map((order) => order >= 0));
  protected readonly sortOrderError$: Observable<boolean> = combineLatest([this.sortOrderTouched$, this.sortOrderValid$]).pipe(
    map(([touched, valid]) => touched && !valid),
  );

  private readonly formValid$: Observable<boolean> = combineLatest([this.hasImage$, this.sortOrderValid$]).pipe(
    map(([hasImage, sortOrderValid]) => hasImage && sortOrderValid),
  );

  protected readonly creating$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly cannotSubmit$: Observable<boolean> = combineLatest([this.formValid$, this.creating$]).pipe(
    map(([formValid, creating]) => !formValid || creating),
  );

  protected readonly ButtonSize = ButtonSize;

  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly highlightsService: HighlightsService,
    private readonly notificationService: NotificationService,
    private readonly dialogService: DialogService,
    private readonly assetsService: AssetsService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly environmentInjector: EnvironmentInjector,
  ) {}

  ngOnDestroy(): void {
    this.imageId$.complete();
    this.sortOrder$.complete();
    this.sortOrderTouched$.complete();
    this.creating$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  selectImage(): void {
    const dialog = Dialog.create<AssetSelectDialogResult>({
      title: 'Bild auswählen',
      componentType: AssetSelectDialog,
      providers: [
        {
          provide: AssetSelectDialogData,
          useValue: AssetSelectDialogData.of({
            multiple: false,
            watermark: false,
            initialContentTypes: ['image/png', 'image/jpeg'],
          }),
        },
        { provide: AssetsService, useValue: this.assetsService },
      ],
      environmentInjector: this.environmentInjector,
    });
    this.dialogService.open(dialog);
    this.dialogService
      .waitUntilClosed(dialog.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        dialog.getResult().ifSome((result) => {
          this.imageId$.next(some(result.assetIds[0]));
        });
      });
  }

  clearImage(): void {
    this.imageId$.next(none());
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  updateSortOrder(value: string): void {
    const sortOrder = parseInt(value, 10);
    this.sortOrder$.next(isNaN(sortOrder) ? 0 : sortOrder);

    if (!this.sortOrderTouched$.value) {
      this.sortOrderTouched$.next(true);
    }
  }

  create(): void {
    const imageId = this.imageId$.value;
    const sortOrder = this.sortOrder$.value;

    if (imageId.isNone()) {
      return;
    }

    this.creating$.next(true);
    this.highlightsService
      .createHighlight({
        imageId: imageId.orElseThrow(),
        sortOrder,
      })
      .pipe(
        delay(500),
        takeUntil(this.destroy$),
        finalize(() => this.creating$.next(false)),
      )
      .subscribe({
        next: (highlightId) => {
          this.notificationService.publish({
            type: 'success',
            message: 'Das Highlight wurde erstellt.',
          });
          this.router.navigate(['..', highlightId], {
            relativeTo: this.route,
          });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Das Highlight konnte nicht erstellt werden.',
          });
        },
      });
  }
}
