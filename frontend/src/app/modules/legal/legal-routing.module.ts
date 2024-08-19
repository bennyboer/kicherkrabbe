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
    title: 'Allgemeine Geschäftsbedingungen',
    component: TermsAndConditionsPage,
  },
  {
    path: 'privacy-policy',
    title: 'Datenschutzerklärung',
    component: PrivacyPolicyPage,
  },
  {
    path: 'imprint',
    title: 'Impressum',
    component: ImprintPage,
  },
  {
    path: 'cancellation-policy',
    title: 'Widerrufsbelehrung',
    component: CancellationPolicyPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class LegalRoutingModule {}
