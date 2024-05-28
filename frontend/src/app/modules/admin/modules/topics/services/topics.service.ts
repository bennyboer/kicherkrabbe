import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
  BehaviorSubject,
  combineLatest,
  map,
  Observable,
  ReplaySubject,
  Subject,
  takeUntil,
} from 'rxjs';
import { Topic } from '../model';
import { environment } from '../../../../../../environments';
import { AdminAuthService } from '../../../services';
import { SSE } from 'sse.js';
import { Option, someOrNone } from '../../../../../util';

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

interface CreateTopicRequest {
  name: string;
}

interface TopicChangeDTO {
  type: string;
  affected: string[];
  payload: any;
}

interface UpdateTopicRequest {
  version: number;
  name: string;
}

@Injectable()
export class TopicsService implements OnDestroy {
  private readonly loadingTopics$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly failedLoadingTopics$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly topics$: Subject<Topic[]> = new ReplaySubject<Topic[]>(1);
  private readonly events$: Subject<TopicChangeDTO> =
    new Subject<TopicChangeDTO>();
  private readonly destroy$: Subject<void> = new Subject<void>();
  private readonly sse: SSE;

  constructor(
    private readonly http: HttpClient,
    private readonly authService: AdminAuthService,
  ) {
    this.reloadTopics();

    const token = this.authService.getCurrentToken().orElseThrow();
    this.sse = new SSE(`${environment.apiUrl}/topics/changes`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    this.sse.onmessage = (event) => {
      const change = JSON.parse(event.data);
      this.events$.next(change);
    };

    this.events$.pipe(takeUntil(this.destroy$)).subscribe((event) => {
      if (event.type === 'CREATED') {
        return; // Ignore since the more important event is PERMISSIONS_ADDED
      }

      this.reloadTopics();
    });
  }

  ngOnDestroy(): void {
    this.loadingTopics$.complete();
    this.failedLoadingTopics$.complete();
    this.topics$.complete();
    this.events$.complete();

    this.destroy$.next();
    this.destroy$.complete();

    this.sse.close();
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

  getTopic(id: string): Observable<Option<Topic>> {
    return this.topics$
      .asObservable()
      .pipe(
        map((topics) => someOrNone(topics.find((topic) => topic.id === id))),
      );
  }

  createTopic(name: string): Observable<void> {
    const request: CreateTopicRequest = { name };

    return this.http.post<void>(`${environment.apiUrl}/topics/create`, request);
  }

  updateTopicName(id: string, version: number, name: string): Observable<void> {
    const request: UpdateTopicRequest = { version, name };

    return this.http.post<void>(
      `${environment.apiUrl}/topics/${id}/update`,
      request,
    );
  }

  deleteTopic(id: string, version: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/topics/${id}/`, {
      params: { version: version.toString() },
    });
  }

  reloadTopics(): void {
    this.loadingTopics$.next(true);
    this.http
      .get<QueryTopicsResponse>(`${environment.apiUrl}/topics/`)
      .pipe(
        map((response) => this.toInternalTopics(response.topics)),
        map((topics) => topics.sort((a, b) => a.name.localeCompare(b.name))),
      )
      .subscribe({
        next: (topics) => {
          this.topics$.next(topics);
          this.loadingTopics$.next(false);
        },
        error: () => {
          this.loadingTopics$.next(false);
          this.failedLoadingTopics$.next(true);
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
