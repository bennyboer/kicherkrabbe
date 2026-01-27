import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WeddingRoutingModule } from './wedding-routing.module';
import { PAGES } from './pages';

@NgModule({
  declarations: [...PAGES],
  imports: [CommonModule, WeddingRoutingModule],
})
export class WeddingModule {}
