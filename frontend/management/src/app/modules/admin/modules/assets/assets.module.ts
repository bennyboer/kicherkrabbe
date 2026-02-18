import { NgModule } from '@angular/core';
import { AssetBrowserComponent, ImageUploadComponent } from './components';
import { AssetSelectDialog } from './dialogs';
import { AssetsService } from './services/assets.service';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

const COMPONENTS = [ImageUploadComponent, AssetBrowserComponent];
const DIALOGS = [AssetSelectDialog];

@NgModule({
  declarations: [...COMPONENTS, ...DIALOGS],
  exports: [ImageUploadComponent, AssetBrowserComponent, AssetSelectDialog],
  imports: [CommonModule, SharedModule],
  providers: [AssetsService, provideHttpClient(withInterceptorsFromDi())],
})
export class AssetsModule {}
