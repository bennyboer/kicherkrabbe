import { ChangeDetectionStrategy, Component, OnDestroy, ViewChild } from '@angular/core';
import { BehaviorSubject, Subject, takeUntil } from 'rxjs';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { AssetsService } from '../../services/assets.service';
import { AssetBrowserComponent } from '../../components';
import { validateProps } from '@kicherkrabbe/shared';

export class AssetSelectDialogData {
  readonly multiple: boolean;
  readonly watermark: boolean;
  readonly initialContentTypes: string[];

  private constructor(props: { multiple: boolean; watermark: boolean; initialContentTypes: string[] }) {
    validateProps(props);

    this.multiple = props.multiple;
    this.watermark = props.watermark;
    this.initialContentTypes = props.initialContentTypes;
  }

  static of(props: {
    multiple?: boolean;
    watermark?: boolean;
    initialContentTypes?: string[];
  }): AssetSelectDialogData {
    return new AssetSelectDialogData({
      multiple: props.multiple ?? false,
      watermark: props.watermark ?? true,
      initialContentTypes: props.initialContentTypes ?? [],
    });
  }
}

export interface AssetSelectDialogResult {
  assetIds: string[];
}

@Component({
  selector: 'app-asset-select-dialog',
  templateUrl: './asset-select.dialog.html',
  styleUrls: ['./asset-select.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class AssetSelectDialog implements OnDestroy {
  @ViewChild('browser')
  browser!: AssetBrowserComponent;

  protected readonly uploadActive$ = new BehaviorSubject<boolean>(false);

  private readonly destroy$ = new Subject<void>();

  constructor(
    protected readonly data: AssetSelectDialogData,
    private readonly dialog: Dialog<AssetSelectDialogResult>,
    private readonly dialogService: DialogService,
    private readonly assetsService: AssetsService,
  ) {}

  ngOnDestroy(): void {
    this.uploadActive$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  activateUpload(): void {
    this.uploadActive$.next(true);
  }

  cancelUpload(): void {
    this.uploadActive$.next(false);
  }

  onImagesUploaded(assetIds: string[]): void {
    this.uploadActive$.next(false);
    this.browser.refresh();
  }

  onAssetSelected(assetId: string): void {
    if (!this.data.multiple) {
      this.dialog.attachResult({ assetIds: [assetId] });
      this.dialogService.close(this.dialog.id);
    }
  }

  confirmSelection(): void {
    const assetIds = this.browser.getSelectedIds();
    if (assetIds.length > 0) {
      this.dialog.attachResult({ assetIds });
      this.dialogService.close(this.dialog.id);
    }
  }

  cancel(): void {
    this.dialogService.close(this.dialog.id);
  }
}
