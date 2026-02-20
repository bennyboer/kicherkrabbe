import { NgModule } from '@angular/core';
import { ImageSliderComponent, SlidingImageComponent, ThumbnailComponent } from './components';
import { CommonModule } from '@angular/common';
import { AuthImagePipe } from '../../pipes/auth-image.pipe';

@NgModule({
  declarations: [ImageSliderComponent, ThumbnailComponent, SlidingImageComponent],
  imports: [CommonModule, AuthImagePipe],
  exports: [ImageSliderComponent],
})
export class ImageSliderModule {}
