import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TelegramRoutingModule } from './telegram-routing.module';
import { SharedModule } from '../../../shared/shared.module';
import { PAGES } from './pages';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { SERVICES } from './services';
import { OptionModule } from '../../../shared/modules/option';

@NgModule({
  imports: [CommonModule, TelegramRoutingModule, SharedModule, OptionModule],
  declarations: [...PAGES],
  providers: [provideHttpClient(withInterceptorsFromDi()), ...SERVICES],
})
export class TelegramModule {}
