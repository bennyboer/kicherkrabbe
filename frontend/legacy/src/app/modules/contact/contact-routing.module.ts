import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ContactPage, SentPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: ContactPage,
  },
  {
    path: 'sent',
    component: SentPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ContactRoutingModule {}
