import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CatalogPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: CatalogPage,
  },
  {
    path: 'patterns',
    title: 'Schnitte',
    loadChildren: () =>
      import('./modules/patterns/patterns.module').then(
        (m) => m.PatternsModule,
      ),
  },
  {
    path: 'fabrics',
    title: 'Stoffe',
    loadChildren: () =>
      import('./modules/fabrics/fabrics.module').then((m) => m.FabricsModule),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CatalogRoutingModule {}
