import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AdminAuthService } from './auth.service';
import { catchError, EMPTY, map, mergeMap, Observable, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { NotificationService } from '../../shared';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private readonly authService: AdminAuthService,
    private readonly notificationService: NotificationService,
    private readonly router: Router,
  ) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler,
  ): Observable<HttpEvent<any>> {
    return this.authService.getToken().pipe(
      map((token) =>
        token
          .map((token: string) =>
            req.clone({
              setHeaders: {
                Authorization: `Bearer ${token}`,
              },
            }),
          )
          .orElse(req),
      ),
      mergeMap((req) => next.handle(req)),
      catchError((e) => {
        const isUnauthorized = e?.status === 401;
        if (isUnauthorized) {
          this.authService.logout();
          this.notificationService.publish({
            message:
              'Ihre Anmeldung ist abgelaufen. Bitte melden Sie sich erneut an.',
            type: 'error',
          });
          this.router.navigate(['/admin/login'], {
            queryParams: {
              redirect: this.router.url,
            },
          });

          return EMPTY;
        }

        return throwError(() => e);
      }),
    );
  }
}
