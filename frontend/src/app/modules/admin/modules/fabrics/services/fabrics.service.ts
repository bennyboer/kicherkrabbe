import { Injectable, OnDestroy } from '@angular/core';
import {
  BehaviorSubject,
  combineLatest,
  debounceTime,
  distinctUntilChanged,
  filter,
  map,
  Observable,
  of,
  ReplaySubject,
  Subject,
  takeUntil,
  tap,
} from 'rxjs';
import { none, Option, some, someOrNone } from '../../../../../util';
import { SSE } from 'sse.js';
import { HttpClient } from '@angular/common/http';
import { AdminAuthService } from '../../../services';
import { environment } from '../../../../../../environments';
import {
  ColorId,
  Fabric,
  FabricId,
  FabricTopic,
  FabricTypeAvailability,
  ImageId,
  TopicId,
} from '../model';

interface FabricDTO {
  id: string;
  version: number;
  name: string;
  imageId: string;
  colorIds: string[];
  topicIds: string[];
  availability: FabricTypeAvailabilityDTO[];
  published: boolean;
  createdAt: string;
}

interface FabricTypeAvailabilityDTO {
  typeId: string;
  inStock: boolean;
}

interface FabricChangeDTO {
  type: string;
  affected: string[];
  payload: any;
}

interface CreateFabricRequest {
  name: string;
  imageId: string;
  colorIds: string[];
  topicIds: string[];
  availability: FabricTypeAvailabilityDTO[];
}

interface QueryFabricsRequest {
  searchTerm: string;
  skip: number;
  limit: number;
}

interface QueryFabricsResponse {
  skip: number;
  limit: number;
  total: number;
  fabrics: FabricDTO[];
}

interface TopicDTO {
  id: string;
  name: string;
}

interface QueryTopicsResponse {
  topics: TopicDTO[];
}

@Injectable()
export class FabricsService implements OnDestroy {
  private readonly loadingFabrics$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly failedLoadingFabrics$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly fabrics$: Subject<Fabric[]> = new ReplaySubject<Fabric[]>(1);
  private readonly events$: Subject<FabricChangeDTO> =
    new Subject<FabricChangeDTO>();
  private readonly subscribedToFabrics$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();
  private sse: Option<SSE> = none();
  private fabricsSubCounter: number = 0;

  constructor(
    private readonly http: HttpClient,
    private readonly authService: AdminAuthService,
  ) {
    this.subscribedToFabrics$
      .pipe(distinctUntilChanged(), debounceTime(100), takeUntil(this.destroy$))
      .subscribe((subscribed) => {
        if (subscribed) {
          this.makeSureEventStreamIsOpen();
        } else {
          this.closeEventStream();
        }
      });

    this.events$.pipe(takeUntil(this.destroy$)).subscribe((event) => {
      if (event.type === 'CREATED') {
        return; // Ignore since the more important event is PERMISSIONS_ADDED
      }

      this.reloadFabrics();
    });

    const loggedOut$ = this.authService
      .getToken()
      .pipe(filter((token) => token.isNone()));
    loggedOut$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.closeEventStream();
    });
  }

  ngOnDestroy(): void {
    this.loadingFabrics$.complete();
    this.failedLoadingFabrics$.complete();
    this.fabrics$.complete();
    this.events$.complete();
    this.subscribedToFabrics$.complete();

    this.destroy$.next();
    this.destroy$.complete();

    this.closeEventStream();
  }

  isLoading(): Observable<boolean> {
    return combineLatest([this.loadingFabrics$]).pipe(
      map(([loadingFabrics]) => loadingFabrics),
    );
  }

  isFailedLoadingFabrics(): Observable<boolean> {
    return this.failedLoadingFabrics$.asObservable();
  }

  getFabrics(): Observable<Fabric[]> {
    return this.fabrics$.asObservable().pipe(
      tap({
        subscribe: () => this.onFabricsSubscribed(),
        unsubscribe: () => this.onFabricsUnsubscribed(),
      }),
    );
  }

  getFabric(id: string): Observable<Option<Fabric>> {
    return this.getFabrics().pipe(
      map((fabrics) => someOrNone(fabrics.find((fabric) => fabric.id === id))),
    );
  }

  createFabric(props: {
    name: string;
    image: ImageId;
    colors: Set<ColorId>;
    topics: Set<TopicId>;
    availability: FabricTypeAvailability[];
  }): Observable<void> {
    const request: CreateFabricRequest = {
      name: props.name,
      imageId: props.image,
      colorIds: Array.from(props.colors),
      topicIds: Array.from(props.topics),
      availability: props.availability.map((availability) => ({
        typeId: availability.typeId,
        inStock: availability.inStock,
      })),
    };

    return this.http.post<void>(
      `${environment.apiUrl}/fabrics/create`,
      request,
    );
  }

  deleteFabric(id: string, version: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/fabrics/${id}/`, {
      params: { version: version.toString() },
    });
  }

  updateFabricName(
    id: FabricId,
    version: number,
    name: string,
  ): Observable<void> {
    return of(null as unknown as void); // TODO
  }

  getAvailableTopicsForFabrics(): Observable<FabricTopic[]> {
    return this.http
      .get<QueryTopicsResponse>(`${environment.apiUrl}/fabrics/topics`)
      .pipe(
        map((response) =>
          response.topics.map((topic) =>
            FabricTopic.of({ id: topic.id, name: topic.name }),
          ),
        ),
      );
  }

  private reloadFabrics(): void {
    const request: QueryFabricsRequest = {
      searchTerm: '',
      skip: 0,
      limit: 9999999,
    };

    this.loadingFabrics$.next(true);
    this.http
      .post<QueryFabricsResponse>(`${environment.apiUrl}/fabrics/`, request)
      .pipe(
        map((response) => this.toInternalFabrics(response.fabrics)),
        map((fabrics) => fabrics.sort((a, b) => a.name.localeCompare(b.name))),
      )
      .subscribe({
        next: (fabrics) => {
          this.fabrics$.next(fabrics);
          this.loadingFabrics$.next(false);
        },
        error: () => {
          this.loadingFabrics$.next(false);
          this.failedLoadingFabrics$.next(true);
        },
      });
  }

  private toInternalFabrics(fabrics: FabricDTO[]): Fabric[] {
    return fabrics.map((fabric) => this.toInternalFabric(fabric));
  }

  private toInternalFabric(fabric: FabricDTO): Fabric {
    return Fabric.of({
      id: fabric.id,
      version: fabric.version,
      name: fabric.name,
      image: fabric.imageId,
      colors: new Set(fabric.colorIds),
      topics: new Set(fabric.topicIds),
      availability: fabric.availability.map((availability) =>
        FabricTypeAvailability.of({
          typeId: availability.typeId,
          inStock: availability.inStock,
        }),
      ),
      published: fabric.published,
      createdAt: new Date(fabric.createdAt),
    });
  }

  private onFabricsSubscribed(): void {
    this.fabricsSubCounter++;
    this.onFabricsSubscriptionsChanged();
  }

  private onFabricsUnsubscribed(): void {
    this.fabricsSubCounter--;
    this.onFabricsSubscriptionsChanged();
  }

  private onFabricsSubscriptionsChanged(): void {
    this.subscribedToFabrics$.next(this.fabricsSubCounter > 0);
  }

  private makeSureEventStreamIsOpen(): void {
    if (this.sse.isNone()) {
      this.openEventStream();
    }
  }

  private openEventStream(): void {
    const token = this.authService.getCurrentToken().orElseThrow();

    const sse = new SSE(`${environment.apiUrl}/fabrics/changes`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    sse.onmessage = (event) => {
      const change = JSON.parse(event.data);
      this.events$.next(change);
    };
    sse.onabort = () => this.closeEventStream();
    sse.onerror = () => this.closeEventStream();

    this.sse = some(sse);
    this.reloadFabrics();
  }

  private closeEventStream(): void {
    this.sse.ifSome((sse) => sse.close());
    this.sse = none();
  }
}
