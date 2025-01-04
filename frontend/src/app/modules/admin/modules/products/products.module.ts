import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { ProductsRoutingModule } from './products-routing.module';
import { PAGES } from './pages';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { SERVICES } from './services';
import { OptionModule } from '../../../shared/modules/option';
import { ImageSliderModule } from '../../../shared/modules/image-slider';
import { QuillViewComponent } from 'ngx-quill';

@NgModule({
  imports: [CommonModule, ProductsRoutingModule, SharedModule, OptionModule, ImageSliderModule, QuillViewComponent],
  declarations: [...PAGES],
  providers: [provideHttpClient(withInterceptorsFromDi()), ...SERVICES],
})
export class ProductsModule {}
