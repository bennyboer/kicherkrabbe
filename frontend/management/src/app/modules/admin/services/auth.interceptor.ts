import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AdminAuthService } from './auth.service';
import { BehaviorSubject, catchError, EMPTY, filter, Observable, switchMap, take, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { NotificationService } from '../../shared';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private refreshing = false;
  private readonly refreshResult$: BehaviorSubject<boolean | null> = new BehaviorSubject<boolean | null>(null);

  constructor(
    private readonly authService: AdminAuthService,
    private readonly notificationService: NotificationService,
    private readonly router: Router,
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const authReq = this.addToken(req);

    return next.handle(authReq).pipe(
      catchError((e) => {
        if (e?.status === 401 && !this.isRefreshOrLogoutUrl(req.url)) {
          return this.handle401(req, next);
        }

        if (e?.status === 401) {
          return this.handleAuthFailure();
        }

        return throwError(() => e);
      }),
    );
  }

  private addToken(req: HttpRequest<any>): HttpRequest<any> {
    if (this.isRefreshOrLogoutUrl(req.url)) {
      return req;
    }

    const tokenOpt = this.authService.getCurrentToken();
    return tokenOpt
      .map((token: string) =>
        req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`,
          },
        }),
      )
      .orElse(req);
  }

  private handle401(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.refreshing) {
      this.refreshing = true;
      this.refreshResult$.next(null);

      return this.authService.refreshAccessToken().pipe(
        switchMap((success) => {
          this.refreshing = false;
          this.refreshResult$.next(success);

          if (success) {
            return next.handle(this.addToken(req));
          }

          return this.handleAuthFailure();
        }),
      );
    }

    return this.refreshResult$.pipe(
      filter((result) => result !== null),
      take(1),
      switchMap((success) => {
        if (success) {
          return next.handle(this.addToken(req));
        }
        return this.handleAuthFailure();
      }),
    );
  }

  private handleAuthFailure(): Observable<never> {
    this.authService.logout();
    this.notificationService.publish({
      message: 'Ihre Anmeldung ist abgelaufen. Bitte melden Sie sich erneut an.',
      type: 'error',
    });
    this.router.navigate(['/admin/login'], {
      queryParams: {
        redirect: this.router.url,
      },
    });

    return EMPTY;
  }

  private isRefreshOrLogoutUrl(url: string): boolean {
    return url.includes('/credentials/refresh') || url.includes('/credentials/logout');
  }
}
