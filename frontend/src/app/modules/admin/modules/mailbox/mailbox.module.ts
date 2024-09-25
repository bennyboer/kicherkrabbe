import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MailboxRoutingModule } from './mailbox-routing.module';
import { PAGES } from './pages';
import { SharedModule } from '../../../shared/shared.module';

@NgModule({
  imports: [CommonModule, MailboxRoutingModule, SharedModule],
  declarations: [...PAGES],
})
export class MailboxModule {}
