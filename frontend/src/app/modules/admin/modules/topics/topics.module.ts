import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { TopicsRoutingModule } from './topics-routing.module';
import { TopicsPage } from './pages';
import { TopicsService } from './services';
import { HttpClientModule } from '@angular/common/http';

const PAGES = [TopicsPage];

@NgModule({
  declarations: [...PAGES],
  imports: [TopicsRoutingModule, CommonModule, SharedModule, HttpClientModule],
  providers: [TopicsService],
})
export class TopicsModule {}
