import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { SharedModule } from '../../../shared/shared.module';
import { EmbroideriesRoutingModule } from './embroideries-routing.module';
import { PAGES } from './pages';
import { SERVICES } from './services';
import { OptionModule } from '../../../shared/modules/option';
import { DialogModule } from '../../../shared/modules/dialog';
import { AssetsModule } from '../assets/assets.module';

@NgModule({
  imports: [CommonModule, EmbroideriesRoutingModule, SharedModule, OptionModule, AssetsModule, DialogModule.forChild()],
  declarations: [...PAGES],
  providers: [provideHttpClient(withInterceptorsFromDi()), ...SERVICES],
})
export class EmbroideriesModule {}
