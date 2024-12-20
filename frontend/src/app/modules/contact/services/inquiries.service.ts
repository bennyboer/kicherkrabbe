import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { InquiriesStatus } from '../models';
import { environment } from '../../../../environments';

interface QueryStatusResponse {
  enabled: boolean;
}

@Injectable()
export class InquiriesService {
  constructor(private readonly http: HttpClient) {}

  getStatus(): Observable<InquiriesStatus> {
    return this.http
      .get<QueryStatusResponse>(`${environment.apiUrl}/inquiries/status`)
      .pipe(
        map((response) => InquiriesStatus.of({ enabled: response.enabled })),
      );
  }
}
