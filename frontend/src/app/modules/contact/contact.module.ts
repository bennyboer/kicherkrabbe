import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PAGES } from './pages';
import { ContactRoutingModule } from './contact-routing.module';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [...PAGES],
  imports: [CommonModule, ContactRoutingModule, SharedModule],
})
export class ContactModule {}
