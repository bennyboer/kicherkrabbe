import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {
  CancellationPolicyPage,
  ImprintPage,
  PrivacyPolicyPage,
  TermsAndConditionsPage,
} from './pages';

const routes: Routes = [
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
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class LegalRoutingModule {}
