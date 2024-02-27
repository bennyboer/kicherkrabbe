import { PatternPage, PatternsPage } from './pages';
import { NgModule } from '@angular/core';
import { PatternsRoutingModule } from './patterns-routing.module';
import { CommonModule } from '@angular/common';

const PAGES = [PatternsPage, PatternPage];

@NgModule({
  declarations: [...PAGES],
  imports: [CommonModule, PatternsRoutingModule],
})
export class PatternsModule {}
