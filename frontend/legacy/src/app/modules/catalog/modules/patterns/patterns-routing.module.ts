import { RouterModule, Routes } from '@angular/router';
import { PatternPage, PatternsPage } from './pages';
import { NgModule } from '@angular/core';

const routes: Routes = [
  {
    path: '',
    component: PatternsPage,
  },
  {
    path: ':id',
    component: PatternPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class PatternsRoutingModule {}
