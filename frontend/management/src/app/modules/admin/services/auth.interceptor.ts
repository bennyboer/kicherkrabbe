import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {AdminAuthService} from './auth.service';
import {BehaviorSubject, catchError, EMPTY, filter, finalize, Observable, switchMap, take, throwError} from 'rxjs';
import {Router} from '@angular/router';
import {environment} from '../../../../environments';
import {none, Option, some} from '@kicherkrabbe/shared';
import {NotificationService} from '../../shared';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private refreshing = false;
  private authFailureHandled = false;
  private readonly refreshResult$ = new BehaviorSubject<Option<boolean>>(none());

  constructor(
    private readonly authService: AdminAuthService,
    private readonly notificationService: NotificationService,
    private readonly router: Router,
  ) {
  }

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
      this.authFailureHandled = false;
      this.refreshResult$.next(none());

      return this.authService.refreshAccessToken().pipe(
        switchMap((success) => {
          this.refreshResult$.next(some(success));

          if (success) {
            return next.handle(this.addToken(req));
          }

          return this.handleAuthFailure();
        }),
        finalize(() => (this.refreshing = false)),
      );
    }

    return this.refreshResult$.pipe(
      filter((result) => result.isSome()),
      take(1),
      switchMap((result) => {
        if (result.orElse(false)) {
          return next.handle(this.addToken(req));
        }
        return EMPTY;
      }),
    );
  }

  private handleAuthFailure(): Observable<never> {
    if (this.authFailureHandled) {
      return EMPTY;
    }
    this.authFailureHandled = true;

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
    return url.startsWith(`${environment.apiUrl}/credentials/refresh`) || url.startsWith(`${environment.apiUrl}/credentials/logout`);
  }
}
