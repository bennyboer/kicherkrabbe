import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: DashboardPage,
  },
  // TODO Login page, etc.
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
