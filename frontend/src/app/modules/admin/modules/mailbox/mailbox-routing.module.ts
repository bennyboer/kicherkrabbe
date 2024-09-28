import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DeletePage, MailboxPage, MailPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: MailboxPage,
  },
  {
    path: ':mailId',
    children: [
      {
        path: '',
        component: MailPage,
      },
      {
        path: 'delete',
        component: DeletePage,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class MailboxRoutingModule {}
