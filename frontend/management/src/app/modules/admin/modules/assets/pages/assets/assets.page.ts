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

  ngOnDestroy(): void {
    this.uploadActive$.complete();
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
}
