import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { CreateFabricPage, FabricDetailsPage, FabricsPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: FabricsPage,
  },
  {
    path: 'create',
    component: CreateFabricPage,
  },
  {
    path: ':id',
    component: FabricDetailsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FabricsRoutingModule {}
