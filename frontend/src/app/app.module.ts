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
import { RoutingMetadataService, ThemeService } from './services';
import { SortablejsModule } from 'nxt-sortablejs';
import { TitleStrategy } from '@angular/router';
import { RoutingTitleStrategy } from './routing-title-strategy';
import { QuillModule } from 'ngx-quill';

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
    QuillModule.forRoot(),
  ],
  providers: [
    ThemeService,
    RoutingMetadataService,
    {
      provide: TitleStrategy,
      useClass: RoutingTitleStrategy,
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
