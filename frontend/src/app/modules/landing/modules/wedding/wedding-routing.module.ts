import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { WeddingPage } from './pages';

const routes: Routes = [
  {
    path: '',
    title:
      'Kleine Gäste, großer Auftritt: Kinder- und Babykleidung für Hochzeiten und besondere Anlässe',
    component: WeddingPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class WeddingRoutingModule {}
