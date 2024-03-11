import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FabricsPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: FabricsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FabricsRoutingModule {}
