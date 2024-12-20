import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../../../environments';
import { RateLimit, RateLimits, Settings } from '../models';

interface QuerySettingsResponse {
  enabled: boolean;
  rateLimits: RateLimitsDTO;
}

interface RateLimitsDTO {
  perMail: RateLimitDTO;
  perIp: RateLimitDTO;
  overall: RateLimitDTO;
}

interface RateLimitDTO {
  maxRequests: number;
  duration: string;
}

interface UpdateRateLimitsRequest {
  rateLimits: RateLimitsDTO;
}

@Injectable()
export class InquiriesService {
  constructor(private readonly http: HttpClient) {}

  enableInquiries(): Observable<void> {
    return this.http.post<void>(
      `${environment.apiUrl}/inquiries/settings/enable`,
      {},
    );
  }

  disableInquiries(): Observable<void> {
    return this.http.post<void>(
      `${environment.apiUrl}/inquiries/settings/disable`,
      {},
    );
  }

  getSettings(): Observable<Settings> {
    return this.http
      .get<QuerySettingsResponse>(`${environment.apiUrl}/inquiries/settings`)
      .pipe(
        map((response) =>
          Settings.of({
            enabled: response.enabled,
            rateLimits: {
              perMail: RateLimit.fullDay(
                response.rateLimits.perMail.maxRequests,
              ),
              perIp: RateLimit.fullDay(response.rateLimits.perIp.maxRequests),
              overall: RateLimit.fullDay(
                response.rateLimits.overall.maxRequests,
              ),
            },
          }),
        ),
      );
  }

  updateRateLimits(rateLimits: RateLimits): Observable<void> {
    const request: UpdateRateLimitsRequest = {
      rateLimits: {
        perMail: {
          maxRequests: rateLimits.perMail.maxRequests,
          duration: 'PT24H',
        },
        perIp: {
          maxRequests: rateLimits.perIp.maxRequests,
          duration: 'PT24H',
        },
        overall: {
          maxRequests: rateLimits.overall.maxRequests,
          duration: 'PT24H',
        },
      },
    };

    return this.http.post<void>(
      `${environment.apiUrl}/inquiries/settings/rate-limits`,
      request,
    );
  }
}
