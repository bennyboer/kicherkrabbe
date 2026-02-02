import { NgModule } from '@angular/core';
import { LoginPage } from './pages';
import { AdminRoutingModule } from './admin-routing.module';
import { AdminAuthService, AuthInterceptor } from './services';
import { SharedModule } from '../shared/shared.module';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ContainerComponent, HeaderComponent } from './components';

const PAGES = [LoginPage];

const COMPONENTS = [ContainerComponent, HeaderComponent];

@NgModule({
  declarations: [...PAGES, ...COMPONENTS],
  imports: [CommonModule, AdminRoutingModule, SharedModule, FormsModule],
  providers: [
    AdminAuthService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
    provideHttpClient(withInterceptorsFromDi()),
  ],
})
export class AdminModule {}
