import { PatternPage, PatternsPage } from './pages';
import { NgModule } from '@angular/core';
import { PatternsRoutingModule } from './patterns-routing.module';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { ImageSliderModule } from '../../../shared/modules/image-slider';
import { PatternCategoriesService, PatternsService } from './services';
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';

const PAGES = [PatternsPage, PatternPage];

@NgModule({
  declarations: [...PAGES],
  imports: [
    CommonModule,
    PatternsRoutingModule,
    SharedModule,
    ImageSliderModule,
  ],
  providers: [
    PatternsService,
    PatternCategoriesService,
    provideHttpClient(withInterceptorsFromDi()),
  ],
})
export class PatternsModule {}
