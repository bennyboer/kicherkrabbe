import { NgModule } from '@angular/core';
import { DashboardPage, LoginPage } from './pages';
import { AdminRoutingModule } from './admin-routing.module';
import { AdminAuthService } from './services';
import { SharedModule } from '../shared/shared.module';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

const PAGES = [DashboardPage, LoginPage];

@NgModule({
  declarations: [...PAGES],
  imports: [
    CommonModule,
    AdminRoutingModule,
    SharedModule,
    FormsModule,
    HttpClientModule,
  ],
  providers: [AdminAuthService],
})
export class AdminModule {}
