import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { ColorsRoutingModule } from './colors-routing.module';
import { ColorsService } from './services';
import { ColorDetailsPage, ColorsPage, CreateColorPage } from './pages';

const PAGES = [ColorsPage, CreateColorPage, ColorDetailsPage];

@NgModule({
  declarations: [...PAGES],
  imports: [ColorsRoutingModule, CommonModule, SharedModule],
  providers: [ColorsService, provideHttpClient(withInterceptorsFromDi())],
})
export class ColorsModule {}
