import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CreatePage, DeletePage, HighlightPage, HighlightsPage } from './pages';

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
    path: ':highlightId',
    children: [
      {
        path: '',
        component: HighlightPage,
      },
      {
        path: 'delete',
        component: DeletePage,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class HighlightsRoutingModule {}
