import { NgModule } from '@angular/core';
import { LegalRoutingModule } from './legal-routing.module';
import { CommonModule } from '@angular/common';
import {
  CancellationPolicyPage,
  ImprintPage,
  PrivacyPolicyPage,
  TermsAndConditionsPage,
} from './pages';

const PAGES = [
  TermsAndConditionsPage,
  ImprintPage,
  PrivacyPolicyPage,
  CancellationPolicyPage,
];

@NgModule({
  declarations: [...PAGES],
  imports: [CommonModule, LegalRoutingModule],
})
export class LegalModule {}
