import { NgModule } from '@angular/core';
import { ImageUploadComponent } from './components';
import { AssetsService } from './services/assets.service';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  declarations: [ImageUploadComponent],
  imports: [CommonModule, SharedModule, HttpClientModule],
  exports: [ImageUploadComponent],
  providers: [AssetsService],
})
export class AssetsModule {}
