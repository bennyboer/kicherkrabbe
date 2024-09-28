import { Injectable, OnDestroy } from '@angular/core';
import {
  BehaviorSubject,
  combineLatest,
  debounceTime,
  distinctUntilChanged,
  filter,
  map,
  Observable,
  ReplaySubject,
  Subject,
  takeUntil,
  tap,
} from 'rxjs';
import { SSE } from 'sse.js';
import { HttpClient } from '@angular/common/http';
import { AdminAuthService } from '../../../services';
import { environment } from '../../../../../../environments';
import { FabricType } from '../model';
import {
  none,
  Option,
  some,
  someOrNone,
} from '../../../../shared/modules/option';

interface FabricTypeDTO {
  id: string;
  version: number;
  name: string;
  createdAt: string;
}

interface FabricTypeChangeDTO {
  type: string;
  affected: string[];
  payload: any;
}

interface UpdateFabricTypeRequest {
  version: number;
  name: string;
}

interface CreateFabricTypeRequest {
  name: string;
}

interface QueryFabricTypesResponse {
  skip: number;
  limit: number;
  total: number;
  fabricTypes: FabricTypeDTO[];
}

@Injectable()
export class FabricTypesService implements OnDestroy {
  private readonly loadingFabricTypes$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly failedLoadingFabricTypes$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly fabricTypes$: Subject<FabricType[]> = new ReplaySubject<
    FabricType[]
  >(1);
  private readonly events$: Subject<FabricTypeChangeDTO> =
    new Subject<FabricTypeChangeDTO>();
  private readonly subscribedToFabricTypes$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();
  private sse: Option<SSE> = none();
  private fabricTypesSubCounter: number = 0;

  constructor(
    private readonly http: HttpClient,
    private readonly authService: AdminAuthService,
  ) {
    this.subscribedToFabricTypes$
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

      this.reloadFabricTypes();
    });

    const loggedOut$ = this.authService
      .getToken()
      .pipe(filter((token) => token.isNone()));
    loggedOut$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.closeEventStream();
    });
  }

  ngOnDestroy(): void {
    this.loadingFabricTypes$.complete();
    this.failedLoadingFabricTypes$.complete();
    this.fabricTypes$.complete();
    this.events$.complete();
    this.subscribedToFabricTypes$.complete();

    this.destroy$.next();
    this.destroy$.complete();

    this.closeEventStream();
  }

  isLoading(): Observable<boolean> {
    return combineLatest([this.loadingFabricTypes$]).pipe(
      map(([loadingFabricTypes]) => loadingFabricTypes),
    );
  }

  isFailedLoadingFabricTypes(): Observable<boolean> {
    return this.failedLoadingFabricTypes$.asObservable();
  }

  getFabricTypes(): Observable<FabricType[]> {
    return this.fabricTypes$.asObservable().pipe(
      tap({
        subscribe: () => this.onFabricTypesSubscribed(),
        unsubscribe: () => this.onFabricTypesUnsubscribed(),
      }),
    );
  }

  getFabricType(id: string): Observable<Option<FabricType>> {
    return this.getFabricTypes().pipe(
      map((fabricTypes) =>
        someOrNone(fabricTypes.find((fabricType) => fabricType.id === id)),
      ),
    );
  }

  createFabricType(name: string): Observable<void> {
    const request: CreateFabricTypeRequest = { name };

    return this.http.post<void>(
      `${environment.apiUrl}/fabric-types/create`,
      request,
    );
  }

  updateFabricTypeName(
    id: string,
    version: number,
    name: string,
  ): Observable<void> {
    const request: UpdateFabricTypeRequest = { version, name };

    return this.http.post<void>(
      `${environment.apiUrl}/fabric-types/${id}/update`,
      request,
    );
  }

  deleteFabricType(id: string, version: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/fabric-types/${id}/`, {
      params: { version: version.toString() },
    });
  }

  private reloadFabricTypes(): void {
    this.loadingFabricTypes$.next(true);
    this.http
      .get<QueryFabricTypesResponse>(`${environment.apiUrl}/fabric-types/`)
      .pipe(
        map((response) => this.toInternalFabricTypes(response.fabricTypes)),
        map((fabricTypes) =>
          fabricTypes.sort((a, b) => a.name.localeCompare(b.name)),
        ),
      )
      .subscribe({
        next: (fabricTypes) => {
          this.fabricTypes$.next(fabricTypes);
          this.loadingFabricTypes$.next(false);
        },
        error: () => {
          this.loadingFabricTypes$.next(false);
          this.failedLoadingFabricTypes$.next(true);
        },
      });
  }

  private toInternalFabricTypes(fabricTypes: FabricTypeDTO[]): FabricType[] {
    return fabricTypes.map((fabricType) =>
      this.toInternalFabricType(fabricType),
    );
  }

  private toInternalFabricType(fabricType: FabricTypeDTO): FabricType {
    return FabricType.of({
      id: fabricType.id,
      version: fabricType.version,
      name: fabricType.name,
      createdAt: new Date(fabricType.createdAt),
    });
  }

  private onFabricTypesSubscribed(): void {
    this.fabricTypesSubCounter++;
    this.onFabricTypesSubscriptionsChanged();
  }

  private onFabricTypesUnsubscribed(): void {
    this.fabricTypesSubCounter--;
    this.onFabricTypesSubscriptionsChanged();
  }

  private onFabricTypesSubscriptionsChanged(): void {
    this.subscribedToFabricTypes$.next(this.fabricTypesSubCounter > 0);
  }

  private makeSureEventStreamIsOpen(): void {
    if (this.sse.isNone()) {
      this.openEventStream();
    }
  }

  private openEventStream(): void {
    const token = this.authService.getCurrentToken().orElseThrow();

    const sse = new SSE(`${environment.apiUrl}/fabric-types/changes`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    sse.onmessage = (event) => {
      const change = JSON.parse(event.data);
      this.events$.next(change);
    };
    sse.onabort = () => this.closeEventStream();
    sse.onerror = () => this.closeEventStream();

    this.sse = some(sse);
    this.reloadFabricTypes();
  }

  private closeEventStream(): void {
    this.sse.ifSome((sse) => sse.close());
    this.sse = none();
  }
}
