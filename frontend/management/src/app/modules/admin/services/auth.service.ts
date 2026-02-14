import {Injectable, NgZone, OnDestroy} from '@angular/core';
import {
  BehaviorSubject,
  catchError,
  finalize,
  map,
  Observable,
  of,
  shareReplay,
  skip,
  Subject,
  take,
  takeUntil
} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environments';
import {none, Option, some, someOrNone} from '@kicherkrabbe/shared';

interface AuthTokenResponse {
  token: string;
  refreshToken: string;
}

@Injectable()
export class AdminAuthService implements OnDestroy {
  private readonly token$: BehaviorSubject<Option<string>> = new BehaviorSubject<Option<string>>(none());
  private readonly refreshToken$: BehaviorSubject<Option<string>> = new BehaviorSubject<Option<string>>(none());
  private readonly initialized$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();
  private refreshTimerId: ReturnType<typeof setTimeout> | null = null;
  private refreshInFlight$: Observable<boolean> | null = null;

  constructor(
    private readonly http: HttpClient,
    private readonly ngZone: NgZone,
  ) {
    this.tryToRestoreToken();

    this.refreshToken$.pipe(skip(1), takeUntil(this.destroy$)).subscribe((token) => this.updateRefreshTokenInStorage(token));
  }

  ngOnDestroy(): void {
    this.cancelRefreshTimer();
    this.token$.complete();
    this.refreshToken$.complete();
    this.initialized$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getToken(): Observable<Option<string>> {
    return this.token$.asObservable();
  }

  getCurrentToken(): Option<string> {
    return this.token$.value;
  }

  isLoggedIn(): Observable<boolean> {
    return this.getToken().pipe(map((token) => token.isSome()));
  }

  isInitialized(): Observable<boolean> {
    return this.initialized$.asObservable();
  }

  logout(): void {
    const refreshTokenValue = this.refreshToken$.value.orElse('');
    if (refreshTokenValue) {
      this.http
        .post(`${environment.apiUrl}/credentials/logout`, {refreshToken: refreshTokenValue})
        .pipe(catchError(() => of(null)))
        .subscribe();
    }

    this.clearAuth();
  }

  login(username: string, password: string): Observable<boolean> {
    return this.http
      .post<AuthTokenResponse>(`${environment.apiUrl}/credentials/use`, {name: username, password})
      .pipe(map((response) => this.handleTokenResponse(response)));
  }

  refreshAccessToken(): Observable<boolean> {
    if (this.refreshInFlight$) {
      return this.refreshInFlight$;
    }

    const refreshTokenValue = this.refreshToken$.value.orElse('');
    if (!refreshTokenValue) {
      return of(false);
    }

    this.refreshInFlight$ = this.http
      .post<AuthTokenResponse>(`${environment.apiUrl}/credentials/refresh`, {refreshToken: refreshTokenValue})
      .pipe(
        map((response) => this.handleTokenResponse(response)),
        catchError(() => {
          this.clearAuth();
          return of(false);
        }),
        finalize(() => (this.refreshInFlight$ = null)),
        shareReplay(1),
      );

    return this.refreshInFlight$;
  }

  private handleTokenResponse(response: AuthTokenResponse): boolean {
    this.token$.next(some(response.token));
    this.refreshToken$.next(some(response.refreshToken));
    this.scheduleProactiveRefresh(response.token);
    return true;
  }

  private clearAuth(): void {
    this.cancelRefreshTimer();
    this.token$.next(none());
    this.refreshToken$.next(none());
  }

  private tryToRestoreToken(): void {
    const refreshToken = someOrNone(localStorage.getItem('admin.auth.refreshToken'));

    refreshToken.ifSomeOrElse(
      (rt) => {
        this.refreshToken$.next(some(rt));
        this.refreshAccessToken()
          .pipe(take(1))
          .subscribe(() => this.initialized$.next(true));
      },
      () => {
        this.initialized$.next(true);
      },
    );
  }

  private scheduleProactiveRefresh(accessToken: string): void {
    this.cancelRefreshTimer();

    const expiresAt = this.getTokenExpiry(accessToken);
    if (expiresAt <= 0) return;

    const refreshAt = expiresAt - 60_000;
    const delay = Math.max(refreshAt - Date.now(), 0);

    this.ngZone.runOutsideAngular(() => {
      this.refreshTimerId = setTimeout(() => {
        this.ngZone.run(() => {
          this.refreshAccessToken().subscribe();
        });
      }, delay);
    });
  }

  private cancelRefreshTimer(): void {
    if (this.refreshTimerId !== null) {
      clearTimeout(this.refreshTimerId);
      this.refreshTimerId = null;
    }
  }

  private getTokenExpiry(token: string): number {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000;
    } catch {
      return 0;
    }
  }

  private updateRefreshTokenInStorage(token: Option<string>): void {
    token.ifSomeOrElse(
      (t) => localStorage.setItem('admin.auth.refreshToken', t),
      () => localStorage.removeItem('admin.auth.refreshToken'),
    );
  }
}
