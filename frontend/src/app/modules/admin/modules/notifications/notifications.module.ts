import { NgModule } from '@angular/core';
import { SharedModule } from '../../../shared/shared.module';
import { CommonModule } from '@angular/common';
import { PAGES } from './pages';
import { NotificationsRoutingModule } from './notifications-routing.module';
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { SERVICES } from './services';
import { OptionModule } from '../../../shared/modules/option';
import { COMPONENTS } from './components';

@NgModule({
  imports: [
    CommonModule,
    NotificationsRoutingModule,
    SharedModule,
    OptionModule,
  ],
  declarations: [...PAGES, ...COMPONENTS],
  providers: [provideHttpClient(withInterceptorsFromDi()), ...SERVICES],
})
export class NotificationsModule {}
