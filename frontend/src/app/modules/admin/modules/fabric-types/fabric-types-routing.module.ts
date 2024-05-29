import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import {
  CreateFabricTypePage,
  FabricTypeDetailsPage,
  FabricTypesPage,
} from './pages';

const routes: Routes = [
  {
    path: '',
    component: FabricTypesPage,
  },
  {
    path: 'create',
    component: CreateFabricTypePage,
  },
  {
    path: ':id',
    component: FabricTypeDetailsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class FabricTypesRoutingModule {}
