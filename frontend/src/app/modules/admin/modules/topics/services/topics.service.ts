import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, combineLatest, map, Observable } from 'rxjs';
import { Topic } from '../model';
import { environment } from '../../../../../../environments';

interface QueryTopicsResponse {
  skip: number;
  limit: number;
  total: number;
  topics: TopicDTO[];
}

interface TopicDTO {
  id: string;
  version: number;
  name: string;
  createdAt: string;
}

@Injectable()
export class TopicsService implements OnDestroy {
  private readonly loadingTopics$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly failedLoadingTopics$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly topics$: BehaviorSubject<Topic[]> = new BehaviorSubject<
    Topic[]
  >([]);

  constructor(private readonly http: HttpClient) {
    this.reloadTopics();
  }

  ngOnDestroy(): void {
    this.loadingTopics$.complete();
    this.failedLoadingTopics$.complete();
    this.topics$.complete();
  }

  isLoading(): Observable<boolean> {
    return combineLatest([this.loadingTopics$]).pipe(
      map(([loadingTopics]) => loadingTopics),
    );
  }

  isFailedLoadingTopics(): Observable<boolean> {
    return this.failedLoadingTopics$.asObservable();
  }

  getTopics(): Observable<Topic[]> {
    return this.topics$.asObservable();
  }

  reloadTopics(): void {
    this.loadingTopics$.next(true);
    this.http
      .get<QueryTopicsResponse>(`${environment.apiUrl}/topics/`)
      .pipe(map((response) => this.toInternalTopics(response.topics)))
      .subscribe({
        next: (topics) => {
          this.topics$.next(topics);
          this.loadingTopics$.next(false);
        },
        error: () => {
          this.loadingTopics$.next(false);
        },
      });
  }

  private toInternalTopics(topics: TopicDTO[]): Topic[] {
    return topics.map((topic) => this.toInternalTopic(topic));
  }

  private toInternalTopic(topic: TopicDTO): Topic {
    return Topic.of({
      id: topic.id,
      version: topic.version,
      name: topic.name,
      createdAt: new Date(topic.createdAt),
    });
  }
}
