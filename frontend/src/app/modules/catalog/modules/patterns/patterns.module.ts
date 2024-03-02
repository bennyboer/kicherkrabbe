import { PatternPage, PatternsPage } from './pages';
import { NgModule } from '@angular/core';
import { PatternsRoutingModule } from './patterns-routing.module';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { PatternsStoreService } from './services/store.service';
import { RemotePatternsService } from './services';

const PAGES = [PatternsPage, PatternPage];

@NgModule({
  declarations: [...PAGES],
  imports: [CommonModule, PatternsRoutingModule, SharedModule],
  providers: [PatternsStoreService, RemotePatternsService],
})
export class PatternsModule {}
