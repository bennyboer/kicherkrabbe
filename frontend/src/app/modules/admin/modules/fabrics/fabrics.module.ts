import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { HttpClientModule } from '@angular/common/http';
import { FabricsRoutingModule } from './fabrics-routing.module';
import { FabricsService } from './services';
import { CreateFabricPage, FabricDetailsPage, FabricsPage } from './pages';
import { AssetsModule } from '../assets/assets.module';

const PAGES = [FabricsPage, CreateFabricPage, FabricDetailsPage];

@NgModule({
  declarations: [...PAGES],
  imports: [
    FabricsRoutingModule,
    CommonModule,
    SharedModule,
    HttpClientModule,
    AssetsModule,
  ],
  providers: [FabricsService],
})
export class FabricsModule {}
