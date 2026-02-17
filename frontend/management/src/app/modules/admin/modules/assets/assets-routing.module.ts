import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { AssetsPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: AssetsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AssetsRoutingModule {}
