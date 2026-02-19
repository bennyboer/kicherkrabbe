import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { FabricsRoutingModule } from './fabrics-routing.module';
import { FabricsService } from './services';
import { CreateFabricPage, FabricDetailsPage, FabricsPage } from './pages';
import { AssetsModule } from '../assets/assets.module';
import { DIALOGS } from './dialogs';
import { DialogModule } from '../../../shared/modules/dialog';
import { SortablejsModule } from 'nxt-sortablejs';
import { ImageSliderModule } from '../../../shared/modules/image-slider';

const PAGES = [FabricsPage, CreateFabricPage, FabricDetailsPage];

@NgModule({
  declarations: [...PAGES, ...DIALOGS],
  imports: [FabricsRoutingModule, CommonModule, SharedModule, AssetsModule, DialogModule.forChild(), SortablejsModule, ImageSliderModule],
  providers: [FabricsService, provideHttpClient(withInterceptorsFromDi())],
})
export class FabricsModule {}
