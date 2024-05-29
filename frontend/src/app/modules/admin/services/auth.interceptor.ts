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

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private readonly authService: AdminAuthService,
    private readonly router: Router,
  ) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler,
  ): Observable<HttpEvent<any>> {
    return this.authService.getToken().pipe(
      map((token) =>
        token
          .map((token) =>
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
