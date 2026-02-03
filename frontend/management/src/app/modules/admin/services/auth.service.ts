import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, map, Observable, Subject, takeUntil } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments';
import { none, Option, some, someOrNone } from '@kicherkrabbe/shared';

interface UseCredentialsRequest {
  name: string;
  password: string;
}

interface UseCredentialsResponse {
  token: string;
}

@Injectable()
export class AdminAuthService implements OnDestroy {
  private readonly token$: BehaviorSubject<Option<string>> = new BehaviorSubject<Option<string>>(none());
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(private readonly http: HttpClient) {
    this.tryToRestoreToken();

    this.token$.pipe(takeUntil(this.destroy$)).subscribe((token) => this.updateTokenInStorage(token));
  }

  ngOnDestroy(): void {
    this.token$.complete();

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

  logout(): void {
    this.token$.next(none());
  }

  login(username: string, password: string): Observable<boolean> {
    const request: UseCredentialsRequest = { name: username, password };

    return this.http.post<UseCredentialsResponse>(`${environment.apiUrl}/credentials/use`, request).pipe(
      map((response) => {
        this.token$.next(some(response.token));
        return true;
      }),
    );
  }

  private tryToRestoreToken(): void {
    const token = someOrNone(localStorage.getItem('admin.auth.token'));
    this.token$.next(token);
  }

  private updateTokenInStorage(token: Option<string>): void {
    token.ifSomeOrElse(
      (t) => localStorage.setItem('admin.auth.token', t),
      () => localStorage.removeItem('admin.auth.token'),
    );
  }
}
