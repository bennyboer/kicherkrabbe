import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MailboxPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: MailboxPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class MailboxRoutingModule {}
