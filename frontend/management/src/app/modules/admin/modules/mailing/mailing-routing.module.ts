import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MailPage, MailsPage, SettingsPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: MailsPage,
  },
  {
    path: 'settings',
    component: SettingsPage,
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
export class MailingRoutingModule {}
