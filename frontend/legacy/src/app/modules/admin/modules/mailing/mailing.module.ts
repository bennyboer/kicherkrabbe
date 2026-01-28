import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { MailingRoutingModule } from './mailing-routing.module';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { SERVICES } from './services';
import { PAGES } from './pages';
import { OptionModule } from '../../../shared/modules/option';
import { QuillViewComponent } from 'ngx-quill';

@NgModule({
  imports: [CommonModule, MailingRoutingModule, SharedModule, OptionModule, QuillViewComponent],
  declarations: [...PAGES],
  providers: [provideHttpClient(withInterceptorsFromDi()), ...SERVICES],
})
export class MailingModule {}
