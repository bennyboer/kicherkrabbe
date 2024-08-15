import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CreatePage, PatternPage, PatternsPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: PatternsPage,
  },
  {
    path: 'create',
    component: CreatePage,
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
