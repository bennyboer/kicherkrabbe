import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PAGES } from './pages';
import { CategoriesRoutingModule } from './categories-routing.module';

@NgModule({
  declarations: [...PAGES],
  imports: [CommonModule, CategoriesRoutingModule],
})
export class CategoriesModule {}
