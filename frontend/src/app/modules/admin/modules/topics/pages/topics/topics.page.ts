import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { TopicsService } from '../../services';
import { Topic } from '../../model';
import { BehaviorSubject, combineLatest, map, Observable } from 'rxjs';

@Component({
    selector: 'app-topics-page',
    templateUrl: './topics.page.html',
    styleUrls: ['./topics.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class TopicsPage implements OnDestroy {
  private readonly search$: BehaviorSubject<string> = new BehaviorSubject<string>('');

  constructor(private readonly topicsService: TopicsService) {}

  ngOnDestroy(): void {
    this.search$.complete();
  }

  getTopics(): Observable<Topic[]> {
    return combineLatest([this.topicsService.getTopics(), this.search$]).pipe(
      map(([topics, search]) => topics.filter((topic) => topic.name.toLowerCase().includes(search.toLowerCase()))),
    );
  }

  isSearching(): Observable<boolean> {
    return this.search$.pipe(map((search) => search.length > 0));
  }

  isLoading(): Observable<boolean> {
    return this.topicsService.isLoading();
  }

  isFailed(): Observable<boolean> {
    return this.topicsService.isFailedLoadingTopics();
  }

  updateSearch(value: string): void {
    this.search$.next(value.trim());
  }
}
