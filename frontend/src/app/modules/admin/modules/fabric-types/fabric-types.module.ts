import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { HttpClientModule } from '@angular/common/http';
import { FabricTypesRoutingModule } from './fabric-types-routing.module';
import {
  CreateFabricTypePage,
  FabricTypeDetailsPage,
  FabricTypesPage,
} from './pages';
import { FabricTypesService } from './services';

const PAGES = [FabricTypesPage, CreateFabricTypePage, FabricTypeDetailsPage];

@NgModule({
  declarations: [...PAGES],
  imports: [
    FabricTypesRoutingModule,
    CommonModule,
    SharedModule,
    HttpClientModule,
  ],
  providers: [FabricTypesService],
})
export class FabricTypesModule {}
