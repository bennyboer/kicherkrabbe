import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../../../environments';

interface QueryUnreadMailsCountResponse {
  count: number;
}

@Injectable()
export class MailboxService {
  constructor(private readonly http: HttpClient) {}

  getUnreadMailsCount(): Observable<number> {
    return this.http
      .get<QueryUnreadMailsCountResponse>(
        `${environment.apiUrl}/mailbox/mails/unread/count`,
      )
      .pipe(map((response) => response.count));
  }
}
