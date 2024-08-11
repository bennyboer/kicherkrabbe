import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PatternsRoutingModule } from './patterns-routing.module';
import { PAGES } from './pages';
import { SharedModule } from '../../../shared/shared.module';
import { AssetsModule } from '../assets/assets.module';
import { SortablejsModule } from 'nxt-sortablejs';
import { COMPONENTS } from './components';
import { PatternCategoriesService, PatternsService } from './services';

@NgModule({
  declarations: [...PAGES, ...COMPONENTS],
  imports: [
    CommonModule,
    PatternsRoutingModule,
    SharedModule,
    AssetsModule,
    SortablejsModule,
  ],
  providers: [PatternsService, PatternCategoriesService],
})
export class PatternsModule {}
