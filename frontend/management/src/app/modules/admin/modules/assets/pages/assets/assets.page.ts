import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { BehaviorSubject, map, Subject, takeUntil } from 'rxjs';
import { none, Option, some } from '@kicherkrabbe/shared';
import { AssetBrowserComponent } from '../../components';
import { AssetsService, StorageInfoResponse } from '../../services/assets.service';

@Component({
  selector: 'app-assets-page',
  templateUrl: './assets.page.html',
  styleUrls: ['./assets.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class AssetsPage implements OnInit, OnDestroy {
  @ViewChild('browser')
  browser!: AssetBrowserComponent;

  protected readonly uploadActive$ = new BehaviorSubject<boolean>(false);
  protected readonly watermark$ = new BehaviorSubject<boolean>(true);
  protected readonly storageInfo$ = new BehaviorSubject<Option<StorageInfoResponse>>(none());
  protected readonly uploadDisabled$ = this.storageInfo$.pipe(
    map((info) => info.map((i) => i.limitBytes > 0 && i.usedBytes >= i.limitBytes).orElse(false)),
  );

  private readonly destroy$ = new Subject<void>();

  constructor(private readonly assetsService: AssetsService) {}

  ngOnInit(): void {
    this.loadStorageInfo();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.uploadActive$.complete();
    this.watermark$.complete();
    this.storageInfo$.complete();
  }

  activateUpload(): void {
    this.uploadActive$.next(true);
  }

  cancelUpload(): void {
    this.uploadActive$.next(false);
  }

  onWatermarkChanged(value: boolean): void {
    this.watermark$.next(value);
  }

  onImagesUploaded(assetIds: string[]): void {
    this.uploadActive$.next(false);
    this.browser.refresh();
    this.loadStorageInfo();
  }

  formatBytes(bytes: number): string {
    if (bytes < 1024) {
      return `${bytes} B`;
    } else if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} KB`;
    } else if (bytes < 1024 * 1024 * 1024) {
      return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    }
    return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)} GB`;
  }

  getUsagePercent(info: StorageInfoResponse): number {
    if (info.limitBytes <= 0) {
      return 0;
    }
    return Math.min(100, (info.usedBytes / info.limitBytes) * 100);
  }

  private loadStorageInfo(): void {
    this.assetsService
      .getStorageInfo()
      .pipe(takeUntil(this.destroy$))
      .subscribe((info) => this.storageInfo$.next(some(info)));
  }
}
