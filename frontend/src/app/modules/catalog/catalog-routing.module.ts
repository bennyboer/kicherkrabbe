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
    loadChildren: () =>
      import('./modules/patterns/patterns.module').then(
        (m) => m.PatternsModule,
      ),
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CatalogRoutingModule {}
