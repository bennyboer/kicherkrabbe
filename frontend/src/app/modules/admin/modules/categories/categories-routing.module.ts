import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CategoriesPage, CategoryPage, CreatePage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: CategoriesPage,
  },
  {
    path: 'create',
    component: CreatePage,
  },
  {
    path: ':id',
    component: CategoryPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CategoriesRoutingModule {}
