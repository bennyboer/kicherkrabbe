import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { DashboardRoutingModule } from './dashboard-routing.module';
import { PAGES } from './pages';
import { SERVICES } from './services';

@NgModule({
  declarations: [...PAGES],
  imports: [DashboardRoutingModule, CommonModule, SharedModule],
  providers: [...SERVICES, provideHttpClient(withInterceptorsFromDi())],
})
export class DashboardModule {}
