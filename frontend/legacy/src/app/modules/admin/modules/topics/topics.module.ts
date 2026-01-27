import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { TopicsRoutingModule } from './topics-routing.module';
import { CreateTopicPage, TopicDetailsPage, TopicsPage } from './pages';
import { TopicsService } from './services';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

const PAGES = [TopicsPage, CreateTopicPage, TopicDetailsPage];

@NgModule({
  declarations: [...PAGES],
  imports: [TopicsRoutingModule, CommonModule, SharedModule],
  providers: [TopicsService, provideHttpClient(withInterceptorsFromDi())],
})
export class TopicsModule {}
