import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PAGES } from './pages';
import { ContactRoutingModule } from './contact-routing.module';
import { SharedModule } from '../shared/shared.module';
import { COMPONENTS } from './components';
import { QuillEditorComponent } from 'ngx-quill';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { SERVICES } from './services';

@NgModule({
  declarations: [...PAGES, ...COMPONENTS],
  imports: [CommonModule, ContactRoutingModule, SharedModule, QuillEditorComponent],
  providers: [provideHttpClient(withInterceptorsFromDi()), ...SERVICES],
})
export class ContactModule {}
