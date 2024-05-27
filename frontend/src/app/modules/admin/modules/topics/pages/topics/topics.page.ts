import { ChangeDetectionStrategy, Component } from '@angular/core';
import { TopicsService } from '../../services';
import { Topic } from '../../model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-topics-page',
  templateUrl: './topics.page.html',
  styleUrls: ['./topics.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TopicsPage {
  constructor(private readonly topicsService: TopicsService) {}

  getTopics(): Observable<Topic[]> {
    return this.topicsService.getTopics();
  }
}
