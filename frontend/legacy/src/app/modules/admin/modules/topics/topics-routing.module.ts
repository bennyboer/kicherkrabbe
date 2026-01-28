import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CreateTopicPage, TopicDetailsPage, TopicsPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: TopicsPage,
  },
  {
    path: 'create',
    component: CreateTopicPage,
  },
  {
    path: ':id',
    component: TopicDetailsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TopicsRoutingModule {}
