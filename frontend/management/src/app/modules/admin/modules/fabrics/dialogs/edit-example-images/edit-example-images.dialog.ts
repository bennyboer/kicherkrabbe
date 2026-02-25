import { ChangeDetectionStrategy, Component, EnvironmentInjector, OnDestroy } from '@angular/core';
import { BehaviorSubject, Subject, takeUntil } from 'rxjs';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { environment } from '../../../../../../../environments';
import { someOrNone, validateProps } from '@kicherkrabbe/shared';
import {
  AssetSelectDialog,
  AssetSelectDialogData,
  AssetSelectDialogResult,
} from '../../../assets/dialogs';
import { AssetsService } from '../../../assets/services/assets.service';

type ImageId = string;

export class EditExampleImagesDialogData {
  readonly images: ImageId[];

  private constructor(props: { images: ImageId[] }) {
    validateProps(props);

    this.images = props.images;
  }

  static of(props: { images?: ImageId[] }): EditExampleImagesDialogData {
    return new EditExampleImagesDialogData({
      images: someOrNone(props.images).orElse([]),
    });
  }
}

@Component({
  selector: 'app-edit-example-images-dialog',
  templateUrl: './edit-example-images.dialog.html',
  styleUrls: ['./edit-example-images.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class EditExampleImagesDialog implements OnDestroy {
  protected readonly imageIds$ = new BehaviorSubject<ImageId[]>([]);

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
    private readonly data: EditExampleImagesDialogData,
    private readonly dialog: Dialog<ImageId[]>,
    private readonly dialogService: DialogService,
    private readonly assetsService: AssetsService,
    private readonly environmentInjector: EnvironmentInjector,
  ) {
    this.imageIds$.next(this.data.images);
  }

  ngOnDestroy(): void {
    this.imageIds$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  selectImages(): void {
    const selectDialog = Dialog.create<AssetSelectDialogResult>({
      title: 'Bilder auswählen',
      componentType: AssetSelectDialog,
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
      environmentInjector: this.environmentInjector,
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
    this.dialog.attachResult(this.imageIds$.value);
    this.dialogService.close(this.dialog.id);
  }
}
