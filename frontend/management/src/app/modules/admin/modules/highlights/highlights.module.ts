import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { PAGES } from './pages';
import { HighlightsRoutingModule } from './highlights-routing.module';
import { SharedModule } from '../../../shared/shared.module';
import { SERVICES } from './services';
import { AssetsModule } from '../assets/assets.module';
import { DialogModule } from '../../../shared/modules/dialog';
import { OptionModule } from '../../../shared/modules/option';
import { DIALOGS } from './dialogs';

@NgModule({
  declarations: [...PAGES, ...DIALOGS],
  imports: [CommonModule, HighlightsRoutingModule, SharedModule, AssetsModule, DialogModule.forChild(), OptionModule],
  providers: [provideHttpClient(withInterceptorsFromDi()), ...SERVICES],
})
export class HighlightsModule {}
