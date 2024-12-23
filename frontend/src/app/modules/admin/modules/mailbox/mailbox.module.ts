import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MailboxRoutingModule } from './mailbox-routing.module';
import { PAGES } from './pages';
import { SharedModule } from '../../../shared/shared.module';
import { OptionModule } from '../../../shared/modules/option';
import { QuillViewComponent } from 'ngx-quill';
import { SERVICES } from './services';
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';

@NgModule({
  imports: [
    CommonModule,
    MailboxRoutingModule,
    SharedModule,
    OptionModule,
    QuillViewComponent,
  ],
  declarations: [...PAGES],
  providers: [...SERVICES, provideHttpClient(withInterceptorsFromDi())],
})
export class MailboxModule {}
