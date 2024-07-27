import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PatternsRoutingModule } from './patterns-routing.module';
import { PAGES } from './pages';
import { SharedModule } from '../../../shared/shared.module';
import { AssetsModule } from '../assets/assets.module';
import { SortablejsModule } from 'nxt-sortablejs';

@NgModule({
  declarations: [...PAGES],
  imports: [
    CommonModule,
    PatternsRoutingModule,
    SharedModule,
    AssetsModule,
    SortablejsModule,
  ],
})
export class PatternsModule {}
