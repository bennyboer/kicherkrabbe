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
import { none, Option, some, someOrNone } from '../../../../../util';
import { SSE } from 'sse.js';
import { HttpClient } from '@angular/common/http';
import { AdminAuthService } from '../../../services';
import { environment } from '../../../../../../environments';
import { Color } from '../model';

interface QueryColorsResponse {
  skip: number;
  limit: number;
  total: number;
  colors: ColorDTO[];
}

interface ColorDTO {
  id: string;
  version: number;
  name: string;
  red: number;
  green: number;
  blue: number;
  createdAt: string;
}

interface CreateColorRequest {
  name: string;
  red: number;
  green: number;
  blue: number;
}

interface ColorChangeDTO {
  type: string;
  affected: string[];
  payload: any;
}

interface UpdateColorRequest {
  version: number;
  name: string;
  red: number;
  green: number;
  blue: number;
}

@Injectable()
export class ColorsService implements OnDestroy {
  private readonly loadingColors$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly failedLoadingColors$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly colors$: Subject<Color[]> = new ReplaySubject<Color[]>(1);
  private readonly events$: Subject<ColorChangeDTO> =
    new Subject<ColorChangeDTO>();
  private readonly subscribedToColors$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();
  private sse: Option<SSE> = none();
  private colorsSubCounter: number = 0;

  constructor(
    private readonly http: HttpClient,
    private readonly authService: AdminAuthService,
  ) {
    this.subscribedToColors$
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

      this.reloadColors();
    });

    const loggedOut$ = this.authService
      .getToken()
      .pipe(filter((token) => token.isNone()));
    loggedOut$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.closeEventStream();
    });
  }

  ngOnDestroy(): void {
    this.loadingColors$.complete();
    this.failedLoadingColors$.complete();
    this.colors$.complete();
    this.events$.complete();
    this.subscribedToColors$.complete();

    this.destroy$.next();
    this.destroy$.complete();

    this.closeEventStream();
  }

  isLoading(): Observable<boolean> {
    return combineLatest([this.loadingColors$]).pipe(
      map(([loadingColors]) => loadingColors),
    );
  }

  isFailedLoadingColors(): Observable<boolean> {
    return this.failedLoadingColors$.asObservable();
  }

  getColors(): Observable<Color[]> {
    return this.colors$.asObservable().pipe(
      tap({
        subscribe: () => this.onColorsSubscribed(),
        unsubscribe: () => this.onColorsUnsubscribed(),
      }),
    );
  }

  getColor(id: string): Observable<Option<Color>> {
    return this.getColors().pipe(
      map((colors) => someOrNone(colors.find((color) => color.id === id))),
    );
  }

  createColor(props: {
    name: string;
    red: number;
    green: number;
    blue: number;
  }): Observable<void> {
    const request: CreateColorRequest = {
      name: props.name,
      red: props.red,
      green: props.green,
      blue: props.blue,
    };

    return this.http.post<void>(`${environment.apiUrl}/colors/create`, request);
  }

  updateColor(props: {
    id: string;
    version: number;
    name: string;
    red: number;
    green: number;
    blue: number;
  }): Observable<void> {
    const request: UpdateColorRequest = {
      version: props.version,
      name: props.name,
      red: props.red,
      green: props.green,
      blue: props.blue,
    };

    return this.http.post<void>(
      `${environment.apiUrl}/colors/${props.id}/update`,
      request,
    );
  }

  deleteColor(id: string, version: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/colors/${id}/`, {
      params: { version: version.toString() },
    });
  }

  private reloadColors(): void {
    this.loadingColors$.next(true);
    this.http
      .get<QueryColorsResponse>(`${environment.apiUrl}/colors/`)
      .pipe(
        map((response) => this.toInternalColors(response.colors)),
        map((colors) => colors.sort((a, b) => a.name.localeCompare(b.name))),
      )
      .subscribe({
        next: (colors) => {
          this.colors$.next(colors);
          this.loadingColors$.next(false);
        },
        error: () => {
          this.loadingColors$.next(false);
          this.failedLoadingColors$.next(true);
        },
      });
  }

  private toInternalColors(colors: ColorDTO[]): Color[] {
    return colors.map((color) => this.toInternalColor(color));
  }

  private toInternalColor(color: ColorDTO): Color {
    return Color.of({
      id: color.id,
      version: color.version,
      name: color.name,
      red: color.red,
      green: color.green,
      blue: color.blue,
      createdAt: new Date(color.createdAt),
    });
  }

  private onColorsSubscribed(): void {
    this.colorsSubCounter++;
    this.onColorsSubscriptionsChanged();
  }

  private onColorsUnsubscribed(): void {
    this.colorsSubCounter--;
    this.onColorsSubscriptionsChanged();
  }

  private onColorsSubscriptionsChanged(): void {
    this.subscribedToColors$.next(this.colorsSubCounter > 0);
  }

  private makeSureEventStreamIsOpen(): void {
    if (this.sse.isNone()) {
      this.openEventStream();
    }
  }

  private openEventStream(): void {
    const token = this.authService.getCurrentToken().orElseThrow();

    const sse = new SSE(`${environment.apiUrl}/colors/changes`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    sse.onmessage = (event) => {
      const change = JSON.parse(event.data);
      this.events$.next(change);
    };
    sse.onabort = () => this.closeEventStream();
    sse.onerror = () => this.closeEventStream();

    this.sse = some(sse);
    this.reloadColors();
  }

  private closeEventStream(): void {
    this.sse.ifSome((sse) => sse.close());
    this.sse = none();
  }
}
