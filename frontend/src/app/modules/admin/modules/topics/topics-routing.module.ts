import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TopicsPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: TopicsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TopicsRoutingModule {}
