import { ChangeDetectionStrategy, Component, Injector, OnDestroy } from '@angular/core';
import { BehaviorSubject, finalize, first, Subject, takeUntil } from 'rxjs';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { NotificationService } from '../../../../../shared';
import { OffersService } from '../../services';
import { environment } from '../../../../../../../environments';
import { Option, someOrNone, validateProps } from '@kicherkrabbe/shared';
import {
  AssetSelectDialog,
  AssetSelectDialogData,
  AssetSelectDialogResult,
} from '../../../assets/dialogs';
import { AssetsService } from '../../../assets/services/assets.service';

type ImageId = string;

export class EditImagesDialogData {
  readonly offer: Option<{ id: string; version: number }>;
  readonly images: ImageId[];

  private constructor(props: { offer: Option<{ id: string; version: number }>; images: ImageId[] }) {
    validateProps(props);

    this.offer = props.offer;
    this.images = props.images;
  }

  static of(props: { offer?: { id: string; version: number }; images?: ImageId[] }): EditImagesDialogData {
    return new EditImagesDialogData({
      offer: someOrNone(props.offer),
      images: someOrNone(props.images).orElse([]),
    });
  }
}

export interface EditImagesDialogResult {
  version: number;
  images: ImageId[];
}

@Component({
  selector: 'app-edit-images-dialog',
  templateUrl: './edit-images.dialog.html',
  styleUrls: ['./edit-images.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class EditImagesDialog implements OnDestroy {
  protected readonly saving$ = new BehaviorSubject<boolean>(false);
  protected readonly imageIds$ = new BehaviorSubject<ImageId[]>([]);

  protected readonly cannotSave$ = this.saving$.asObservable();
  protected readonly loading$ = this.saving$.asObservable();

  protected readonly imagesSortableConfig: any = {
    delay: 300,
    delayOnTouchOnly: true,
    touchStartThreshold: 10,
    onUpdate: () => {
      this.imageIds$.next(this.imageIds$.value);
    },
  };

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly data: EditImagesDialogData,
    private readonly dialog: Dialog<EditImagesDialogResult>,
    private readonly dialogService: DialogService,
    private readonly offersService: OffersService,
    private readonly assetsService: AssetsService,
    private readonly notificationService: NotificationService,
  ) {
    this.imageIds$.next(this.data.images);
  }

  ngOnDestroy(): void {
    this.saving$.complete();
    this.imageIds$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  selectImages(): void {
    const selectDialog = Dialog.create<AssetSelectDialogResult>({
      title: 'Bilder auswählen',
      componentType: AssetSelectDialog,
      injector: Injector.create({
        providers: [
          {
            provide: AssetSelectDialogData,
            useValue: AssetSelectDialogData.of({
              multiple: true,
              watermark: false,
              initialContentTypes: ['image/png', 'image/jpeg'],
            }),
          },
          { provide: AssetsService, useValue: this.assetsService },
        ],
      }),
    });
    this.dialogService.open(selectDialog);
    this.dialogService
      .waitUntilClosed(selectDialog.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        selectDialog.getResult().ifSome((result) => {
          this.imageIds$.next([...this.imageIds$.value, ...result.assetIds]);
        });
      });
  }

  deleteImage(imageId: ImageId): void {
    const imageIds = this.imageIds$.value.filter((id) => id !== imageId);
    this.imageIds$.next(imageIds);
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  cancel(): void {
    this.dialogService.close(this.dialog.id);
  }

  save(): void {
    if (this.saving$.value) {
      return;
    }
    this.saving$.next(true);

    const imageIds = this.imageIds$.value;

    this.data.offer.ifSomeOrElse(
      (offer) =>
        this.offersService
          .updateImages({
            id: offer.id,
            version: offer.version,
            imageIds,
          })
          .pipe(
            first(),
            finalize(() => this.saving$.next(false)),
          )
          .subscribe({
            next: (version) => {
              this.notificationService.publish({
                message: 'Bilder wurden erfolgreich aktualisiert',
                type: 'success',
              });

              this.dialog.attachResult({
                version,
                images: imageIds,
              });
              this.dialogService.close(this.dialog.id);
            },
            error: (e) => {
              console.error('Failed to update images', e);
              this.notificationService.publish({
                message: 'Bilder konnten nicht aktualisiert werden. Bitte versuchen Sie es erneut.',
                type: 'error',
              });
            },
          }),
      () => {
        this.dialog.attachResult({
          version: 0,
          images: imageIds,
        });
        this.dialogService.close(this.dialog.id);
      },
    );
  }
}
