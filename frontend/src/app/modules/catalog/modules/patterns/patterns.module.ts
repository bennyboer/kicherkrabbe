import { PatternPage, PatternsPage } from './pages';
import { NgModule } from '@angular/core';
import { PatternsRoutingModule } from './patterns-routing.module';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';

const PAGES = [PatternsPage, PatternPage];

@NgModule({
  declarations: [...PAGES],
  imports: [CommonModule, PatternsRoutingModule, SharedModule],
})
export class PatternsModule {}
