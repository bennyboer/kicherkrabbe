import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppComponent } from './components';
import { NotFoundPage, StartPage } from './pages';

const COMPONENTS = [AppComponent];
const PAGES = [StartPage, NotFoundPage];

@NgModule({
  declarations: [...COMPONENTS, ...PAGES],
  imports: [BrowserModule, BrowserAnimationsModule, AppRoutingModule],
  bootstrap: [AppComponent],
})
export class AppModule {}
