import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { INQUIRY, Mail, Origin, OriginType, Sender, Status, StatusType } from '../model';
import { map, Observable } from 'rxjs';
import { someOrNone } from '../../../../shared/modules/option';
import { environment } from '../../../../../../environments';

interface QueryMailsResponse {
  total: number;
  mails: MailDTO[];
}

interface QueryMailResponse {
  mail: MailDTO;
}

interface MailDTO {
  id: string;
  version: number;
  origin: OriginDTO;
  sender: SenderDTO;
  subject: string;
  content: string;
  receivedAt: string;
  readAt?: string;
}

interface OriginDTO {
  type: OriginTypeDTO;
  id: string;
}

enum OriginTypeDTO {
  INQUIRY = 'INQUIRY',
}

interface SenderDTO {
  name: string;
  mail: string;
  phone?: string;
}

interface MarkAsReadRequest {
  version: number;
}

interface MarkAsReadResponse {
  version: number;
}

interface MarkAsUnreadRequest {
  version: number;
}

interface MarkAsUnreadResponse {
  version: number;
}

interface DeleteMailResponse {
  version: number;
}

export interface QueryMailsResult {
  total: number;
  mails: Mail[];
}

@Injectable()
export class MailboxService {
  constructor(private readonly http: HttpClient) {}

  getMails(props: {
    searchTerm?: string | null;
    status?: Status | null;
    skip?: number | null;
    limit?: number | null;
  }): Observable<QueryMailsResult> {
    const searchTerm = someOrNone(props.searchTerm).orElse('');
    const status = someOrNone(props.status)
      .map((s) => s.type)
      .map((s) => {
        if (s === StatusType.READ) {
          return 'READ';
        }

        return 'UNREAD';
      });
    const skip = someOrNone(props.skip).orElse(0);
    const limit = someOrNone(props.limit).orElse(100);

    let params = new HttpParams()
      .set('searchTerm', searchTerm)
      .set('skip', skip.toString())
      .set('limit', limit.toString());
    params = status.map((s) => params.set('status', s)).orElse(params);

    return this.http
      .get<QueryMailsResponse>(`${environment.apiUrl}/mailbox/mails`, {
        params,
      })
      .pipe(
        map((response) => ({
          total: response.total,
          mails: response.mails.map((mail) => this.toInternalMail(mail)),
        })),
      );
  }

  getMail(mailId: string): Observable<Mail> {
    return this.http
      .get<QueryMailResponse>(`${environment.apiUrl}/mailbox/mails/${mailId}`)
      .pipe(map((response) => this.toInternalMail(response.mail)));
  }

  markMailAsRead(id: string, version: number): Observable<number> {
    const request: MarkAsReadRequest = { version };

    return this.http
      .post<MarkAsReadResponse>(`${environment.apiUrl}/mailbox/mails/${id}/read`, request)
      .pipe(map((response) => response.version));
  }

  markMailAsUnread(id: string, version: number): Observable<number> {
    const request: MarkAsUnreadRequest = { version };

    return this.http
      .post<MarkAsUnreadResponse>(`${environment.apiUrl}/mailbox/mails/${id}/unread`, request)
      .pipe(map((response) => response.version));
  }

  deleteMail(mailId: string, version: number): Observable<number> {
    const params = new HttpParams().set('version', version.toString());

    return this.http
      .delete<DeleteMailResponse>(`${environment.apiUrl}/mailbox/mails/${mailId}`, { params })
      .pipe(map((response) => response.version));
  }

  private toInternalMail(mail: MailDTO): Mail {
    return Mail.of({
      id: mail.id,
      version: mail.version,
      origin: this.toInternalOrigin(mail.origin),
      sender: this.toInternalSender(mail.sender),
      subject: mail.subject,
      content: mail.content,
      receivedAt: new Date(mail.receivedAt),
      read: someOrNone(mail.readAt).isSome(),
    });
  }

  private toInternalSender(sender: SenderDTO): Sender {
    return Sender.of({
      name: sender.name,
      mail: sender.mail,
      phone: someOrNone(sender.phone).orElseNull(),
    });
  }

  private toInternalOrigin(origin: OriginDTO): Origin {
    const originType = this.toInternalOriginType(origin.type);
    return Origin.of({ type: originType, id: origin.id });
  }

  private toInternalOriginType(originType: OriginTypeDTO): OriginType {
    switch (originType) {
      case OriginTypeDTO.INQUIRY:
        return INQUIRY;
      default:
        throw new Error('Unknown origin type');
    }
  }
}
