import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppComponent, HeaderComponent } from './components';
import {
  CancellationPolicyPage,
  ImprintPage,
  NotFoundPage,
  PrivacyPolicyPage,
  StartPage,
  TermsAndConditionsPage,
} from './pages';

const COMPONENTS = [AppComponent, HeaderComponent];
const PAGES = [
  StartPage,
  NotFoundPage,
  TermsAndConditionsPage,
  PrivacyPolicyPage,
  CancellationPolicyPage,
  ImprintPage,
];

@NgModule({
  declarations: [...COMPONENTS, ...PAGES],
  imports: [BrowserModule, BrowserAnimationsModule, AppRoutingModule],
  bootstrap: [AppComponent],
})
export class AppModule {}
