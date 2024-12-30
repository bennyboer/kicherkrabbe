import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TraditionPage } from './pages';

const routes: Routes = [
  {
    path: '',
    title: 'Bayerische Tracht für kleine Herzen – Traditionell und hochwertig, handgemacht in Bayern',
    component: TraditionPage,
    data: {
      description: 'Handgemachte bayerische Trachtenmode für Kinder und Babys',
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TraditionRoutingModule {}
