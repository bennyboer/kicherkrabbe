import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {
  AppComponent,
  DarkModeToggleComponent,
  FooterComponent,
  HeaderComponent,
} from './components';
import { NotFoundPage, StartPage } from './pages';
import { SharedModule } from './modules/shared/shared.module';
import { ThemeService } from './services';
import { SortablejsModule } from 'nxt-sortablejs';

const COMPONENTS = [
  AppComponent,
  HeaderComponent,
  DarkModeToggleComponent,
  FooterComponent,
];
const PAGES = [StartPage, NotFoundPage];

@NgModule({
  declarations: [...COMPONENTS, ...PAGES],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    SharedModule.forRoot(),
    SortablejsModule.forRoot({
      animation: 150,
      handle: '.drag-handle',
    }),
  ],
  providers: [ThemeService],
  bootstrap: [AppComponent],
})
export class AppModule {}
