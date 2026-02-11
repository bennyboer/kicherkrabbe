import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HighlightsPage, HighlightPage, CreatePage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: HighlightsPage,
  },
  {
    path: 'create',
    component: CreatePage,
  },
  {
    path: ':id',
    component: HighlightPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class HighlightsRoutingModule {}
