import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { ColorDetailsPage, ColorsPage, CreateColorPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: ColorsPage,
  },
  {
    path: 'create',
    component: CreateColorPage,
  },
  {
    path: ':id',
    component: ColorDetailsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ColorsRoutingModule {}
