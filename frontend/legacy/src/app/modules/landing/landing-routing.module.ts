import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'hochzeit',
    loadChildren: () => import('./modules/wedding/wedding.module').then((m) => m.WeddingModule),
  },
  {
    path: 'besondere-anlaesse',
    loadChildren: () => import('./modules/wedding/wedding.module').then((m) => m.WeddingModule),
  },
  {
    path: 'tracht',
    loadChildren: () => import('./modules/tradition/tradition.module').then((m) => m.TraditionModule),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class LandingRoutingModule {}
