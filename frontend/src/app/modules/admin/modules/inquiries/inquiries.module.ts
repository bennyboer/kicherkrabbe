import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PAGES } from './pages';
import { InquiriesRoutingModule } from './inquiries-routing.module';
import { SharedModule } from '../../../shared/shared.module';

@NgModule({
  imports: [CommonModule, InquiriesRoutingModule, SharedModule],
  declarations: [...PAGES],
})
export class InquiriesModule {}
