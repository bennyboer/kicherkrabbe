import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FabricPage, FabricsPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: FabricsPage,
  },
  {
    path: ':id',
    component: FabricPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FabricsRoutingModule {}
