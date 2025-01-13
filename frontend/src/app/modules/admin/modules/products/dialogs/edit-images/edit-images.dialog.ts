import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { BehaviorSubject, finalize, first } from 'rxjs';
import { Product } from '../../model';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { NotificationService } from '../../../../../shared';
import { ProductsService } from '../../services';
import { environment } from '../../../../../../../environments';

type ImageId = string;

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
  protected readonly imageUploadActive$ = new BehaviorSubject<boolean>(false);
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

  constructor(
    private readonly product: Product,
    private readonly dialog: Dialog<EditImagesDialogResult>,
    private readonly dialogService: DialogService,
    private readonly productsService: ProductsService,
    private readonly notificationService: NotificationService,
  ) {
    this.imageIds$.next(this.product.images);
  }

  ngOnDestroy(): void {
    this.imageUploadActive$.complete();
    this.saving$.complete();
    this.imageIds$.complete();
  }

  onImagesUploaded(imageIds: string[]): void {
    this.imageUploadActive$.next(false);
    this.imageIds$.next([...this.imageIds$.value, ...imageIds]);
  }

  deleteImage(imageId: ImageId): void {
    const imageIds = this.imageIds$.value.filter((id) => id !== imageId);
    this.imageIds$.next(imageIds);
  }

  activateImageUpload(): void {
    this.imageUploadActive$.next(true);
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

    this.productsService
      .updateImages({
        id: this.product.id,
        version: this.product.version,
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
      });
  }
}
