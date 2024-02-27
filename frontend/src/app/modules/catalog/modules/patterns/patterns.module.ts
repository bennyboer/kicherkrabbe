import { PatternsPage } from './pages';
import { NgModule } from '@angular/core';
import { PatternsRoutingModule } from './patterns-routing.module';

const PAGES = [PatternsPage];

@NgModule({
  declarations: [...PAGES],
  imports: [PatternsRoutingModule],
})
export class PatternsModule {}
