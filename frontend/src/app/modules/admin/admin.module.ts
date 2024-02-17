import { NgModule } from '@angular/core';
import { DashboardPage } from './pages';
import { AdminRoutingModule } from './admin-routing.module';

const PAGES = [DashboardPage];

@NgModule({
  declarations: [...PAGES],
  imports: [AdminRoutingModule],
})
export class AdminModule {}
