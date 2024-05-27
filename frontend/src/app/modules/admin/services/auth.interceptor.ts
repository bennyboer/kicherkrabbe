import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AdminAuthService } from './auth.service';
import { map, mergeMap, Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private readonly authService: AdminAuthService) {}

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
    );
  }
}
