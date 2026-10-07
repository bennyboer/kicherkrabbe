import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CreatePage, DeletePage, EmbroideriesPage, EmbroideryPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: EmbroideriesPage,
  },
  {
    path: 'create',
    component: CreatePage,
  },
  {
    path: ':embroideryId',
    children: [
      {
        path: '',
        component: EmbroideryPage,
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
export class EmbroideriesRoutingModule {}
