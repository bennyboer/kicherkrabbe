import { NgModule } from '@angular/core';
import {
  ImageComponent,
  ImageSliderComponent,
  ThumbnailComponent,
} from './components';
import { CommonModule } from '@angular/common';

@NgModule({
  declarations: [ImageSliderComponent, ImageComponent, ThumbnailComponent],
  imports: [CommonModule],
  exports: [ImageSliderComponent],
})
export class ImageSliderModule {}
