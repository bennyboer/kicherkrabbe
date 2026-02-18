import { ChangeDetectionStrategy, Component, OnDestroy, ViewChild } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { AssetBrowserComponent } from '../../components';

@Component({
  selector: 'app-assets-page',
  templateUrl: './assets.page.html',
  styleUrls: ['./assets.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class AssetsPage implements OnDestroy {
  @ViewChild('browser')
  browser!: AssetBrowserComponent;

  protected readonly uploadActive$ = new BehaviorSubject<boolean>(false);
  protected readonly watermark$ = new BehaviorSubject<boolean>(true);

  ngOnDestroy(): void {
    this.uploadActive$.complete();
    this.watermark$.complete();
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
  }
}
