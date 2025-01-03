import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CreatePage, DeletePage, ProductPage, ProductsPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: ProductsPage,
  },
  {
    path: 'create',
    component: CreatePage,
  },
  {
    path: 'delete',
    component: DeletePage,
  },
  {
    path: ':productId',
    component: ProductPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ProductsRoutingModule {}
