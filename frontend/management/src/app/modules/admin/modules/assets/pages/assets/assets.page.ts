import { ChangeDetectionStrategy, Component, OnDestroy, ViewChild } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { AssetsService } from '../../services/assets.service';
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
  protected readonly uploading$ = new BehaviorSubject<boolean>(false);

  private readonly destroy$ = new Subject<void>();

  constructor(private readonly assetsService: AssetsService) {}

  ngOnDestroy(): void {
    this.uploadActive$.complete();
    this.uploading$.complete();

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
}
