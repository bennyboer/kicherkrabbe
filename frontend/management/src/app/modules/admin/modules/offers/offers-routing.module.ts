import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CreatePage, DeletePage, OfferPage, OffersPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: OffersPage,
  },
  {
    path: 'create',
    component: CreatePage,
  },
  {
    path: ':offerId',
    children: [
      {
        path: '',
        component: OfferPage,
      },
      {
        path: 'delete',
        component: DeletePage,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class OffersRoutingModule {}
