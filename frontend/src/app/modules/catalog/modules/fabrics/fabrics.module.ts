import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FabricsRoutingModule } from './fabrics-routing.module';
import { FabricsPage } from './pages';

const PAGES = [FabricsPage];

@NgModule({
  declarations: [...PAGES],
  imports: [CommonModule, FabricsRoutingModule],
})
export class FabricsModule {}
