import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PAGES } from './pages';
import { InquiriesRoutingModule } from './inquiries-routing.module';
import { SharedModule } from '../../../shared/shared.module';
import { SERVICES } from './services';
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { LineGraphModule } from '../../../shared/modules/line-graph';

@NgModule({
  imports: [
    CommonModule,
    InquiriesRoutingModule,
    SharedModule,
    LineGraphModule,
  ],
  declarations: [...PAGES],
  providers: [provideHttpClient(withInterceptorsFromDi()), ...SERVICES],
})
export class InquiriesModule {}
