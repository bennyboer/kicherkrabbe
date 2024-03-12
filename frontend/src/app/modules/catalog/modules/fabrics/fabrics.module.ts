import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FabricsRoutingModule } from './fabrics-routing.module';
import { FabricPage, FabricsPage } from './pages';
import { SharedModule } from '../../../shared/shared.module';
import { FabricsStoreService, RemoteFabricsService } from './services';

const PAGES = [FabricsPage, FabricPage];

@NgModule({
  declarations: [...PAGES],
  imports: [CommonModule, FabricsRoutingModule, SharedModule],
  providers: [RemoteFabricsService, FabricsStoreService],
})
export class FabricsModule {}
