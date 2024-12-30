import { NgModule } from '@angular/core';
import { ImageUploadComponent } from './components';
import { AssetsService } from './services/assets.service';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

@NgModule({
  declarations: [ImageUploadComponent],
  exports: [ImageUploadComponent],
  imports: [CommonModule, SharedModule],
  providers: [AssetsService, provideHttpClient(withInterceptorsFromDi())],
})
export class AssetsModule {}
