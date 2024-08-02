import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PAGES } from './pages';
import { CategoriesRoutingModule } from './categories-routing.module';
import { SharedModule } from '../../../shared/shared.module';
import { CategoriesService } from './services';

@NgModule({
  declarations: [...PAGES],
  imports: [CommonModule, CategoriesRoutingModule, SharedModule],
  providers: [CategoriesService],
})
export class CategoriesModule {}
