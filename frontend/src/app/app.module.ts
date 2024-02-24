import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {
  AppComponent,
  DarkModeToggleComponent,
  HeaderComponent,
} from './components';
import {
  CancellationPolicyPage,
  ImprintPage,
  NotFoundPage,
  PrivacyPolicyPage,
  StartPage,
  TermsAndConditionsPage,
} from './pages';
import { SharedModule } from './modules/shared/shared.module';
import { ThemeService } from './services';

const COMPONENTS = [AppComponent, HeaderComponent, DarkModeToggleComponent];
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
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    SharedModule,
  ],
  providers: [ThemeService],
  bootstrap: [AppComponent],
})
export class AppModule {}
