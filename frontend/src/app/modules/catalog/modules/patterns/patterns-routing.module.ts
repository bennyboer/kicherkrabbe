import { RouterModule, Routes } from '@angular/router';
import { PatternsPage } from './pages';
import { NgModule } from '@angular/core';

const routes: Routes = [
  {
    path: '',
    component: PatternsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PatternsRoutingModule {}
