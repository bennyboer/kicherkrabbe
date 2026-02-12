import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PAGES } from './pages';
import { HighlightsRoutingModule } from './highlights-routing.module';
import { SharedModule } from '../../../shared/shared.module';
import { HighlightsService } from './services';
import { AssetsModule } from '../assets/assets.module';
import { DialogModule } from '../../../shared/modules/dialog';
import { DIALOGS } from './dialogs';

@NgModule({
  declarations: [...PAGES, ...DIALOGS],
  imports: [CommonModule, HighlightsRoutingModule, SharedModule, AssetsModule, DialogModule.forChild()],
  providers: [HighlightsService],
})
export class HighlightsModule {}
