import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FabricsRoutingModule } from './fabrics-routing.module';
import { FabricPage, FabricsPage } from './pages';
import { SharedModule } from '../../../shared/shared.module';
import { RemoteFabricsService } from './services';
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';

const PAGES = [FabricsPage, FabricPage];

@NgModule({
  declarations: [...PAGES],
  imports: [CommonModule, FabricsRoutingModule, SharedModule],
  providers: [
    RemoteFabricsService,
    provideHttpClient(withInterceptorsFromDi()),
  ],
})
export class FabricsModule {}
