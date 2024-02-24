import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {
  CancellationPolicyPage,
  ImprintPage,
  NotFoundPage,
  PrivacyPolicyPage,
  StartPage,
  TermsAndConditionsPage,
} from './pages';

const routes: Routes = [
  {
    path: 'admin',
    loadChildren: () =>
      import('./modules/admin/admin.module').then((m) => m.AdminModule),
  },
  {
    path: '',
    component: StartPage,
  },
  // TODO Refactor pages to use common header and footer
  {
    path: 'terms-and-conditions',
    component: TermsAndConditionsPage,
  },
  {
    path: 'privacy-policy',
    component: PrivacyPolicyPage,
  },
  {
    path: 'imprint',
    component: ImprintPage,
  },
  {
    path: 'cancellation-policy',
    component: CancellationPolicyPage,
  },
  { path: '**', component: NotFoundPage },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      paramsInheritanceStrategy: 'always',
      scrollPositionRestoration: 'enabled',
    }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
