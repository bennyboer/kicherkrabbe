import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotificationsPage, SettingsPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: NotificationsPage,
  },
  {
    path: 'settings',
    component: SettingsPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class NotificationsRoutingModule {}
