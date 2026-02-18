import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { AssetsRoutingModule } from './assets-routing.module';
import { AssetsModule } from './assets.module';
import { AssetsPage } from './pages';

@NgModule({
  declarations: [AssetsPage],
  imports: [CommonModule, SharedModule, AssetsRoutingModule, AssetsModule],
})
export class AssetsPageModule {}
