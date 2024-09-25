import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MailboxPage, MailPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: MailboxPage,
  },
  {
    path: ':mailId',
    component: MailPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class MailboxRoutingModule {}
