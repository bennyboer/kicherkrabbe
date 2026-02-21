import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { OffersRoutingModule } from './offers-routing.module';
import { PAGES } from './pages';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { SERVICES } from './services';
import { OptionModule } from '../../../shared/modules/option';
import { ImageSliderModule } from '../../../shared/modules/image-slider';
import { QuillEditorComponent, QuillViewComponent } from 'ngx-quill';
import { DialogModule } from '../../../shared/modules/dialog';
import { DIALOGS } from './dialogs';
import { AssetsModule } from '../assets/assets.module';
import { SortablejsModule } from 'nxt-sortablejs';

@NgModule({
  imports: [
    CommonModule,
    OffersRoutingModule,
    SharedModule,
    OptionModule,
    ImageSliderModule,
    AssetsModule,
    QuillViewComponent,
    DialogModule.forChild(),
    QuillEditorComponent,
    SortablejsModule,
  ],
  declarations: [...PAGES, ...DIALOGS],
  providers: [provideHttpClient(withInterceptorsFromDi()), ...SERVICES],
})
export class OffersModule {}
