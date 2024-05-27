import { NgModule } from '@angular/core';
import { DashboardPage, LoginPage } from './pages';
import { AdminRoutingModule } from './admin-routing.module';
import { AdminAuthService, AuthInterceptor } from './services';
import { SharedModule } from '../shared/shared.module';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { ContainerComponent, HeaderComponent } from './components';

const PAGES = [DashboardPage, LoginPage];

const COMPONENTS = [ContainerComponent, HeaderComponent];

@NgModule({
  declarations: [...PAGES, ...COMPONENTS],
  imports: [
    CommonModule,
    AdminRoutingModule,
    SharedModule,
    FormsModule,
    HttpClientModule,
  ],
  providers: [
    AdminAuthService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
  ],
})
export class AdminModule {}
