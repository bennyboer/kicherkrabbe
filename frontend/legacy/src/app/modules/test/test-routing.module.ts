import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SewUiPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: SewUiPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TestRoutingModule {}
