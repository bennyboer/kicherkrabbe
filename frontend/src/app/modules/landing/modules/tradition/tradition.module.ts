import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TraditionRoutingModule } from './tradition-routing.module';
import { PAGES } from './pages';

@NgModule({
  declarations: [...PAGES],
  imports: [CommonModule, TraditionRoutingModule],
})
export class TraditionModule {}
