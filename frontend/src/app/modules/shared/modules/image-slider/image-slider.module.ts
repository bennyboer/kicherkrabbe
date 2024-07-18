import { NgModule } from '@angular/core';
import {
  ImageSliderComponent,
  SlidingImageComponent,
  ThumbnailComponent,
} from './components';
import { CommonModule } from '@angular/common';

@NgModule({
  declarations: [
    ImageSliderComponent,
    ThumbnailComponent,
    SlidingImageComponent,
  ],
  imports: [CommonModule],
  exports: [ImageSliderComponent],
})
export class ImageSliderModule {}
