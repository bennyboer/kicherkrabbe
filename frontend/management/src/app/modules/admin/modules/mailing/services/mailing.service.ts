import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../../../environments';
import { Mail, MailgunSettings, RateLimitSettings, Settings } from '../model';
import { someOrNone } from '@kicherkrabbe/shared';

interface UpdateMailgunApiTokenRequest {
  version: number;
  apiToken: string;
}

interface UpdateMailgunApiTokenResponse {
  version: number;
}

interface ClearMailgunApiTokenRequest {
  version: number;
}

interface ClearMailgunApiTokenResponse {
  version: number;
}

interface UpdateRateLimitRequest {
  version: number;
  durationInMs: number;
  limit: number;
}

interface UpdateRateLimitResponse {
  version: number;
}

interface QuerySettingsResponse {
  settings: SettingsDTO;
}

interface SettingsDTO {
  version: number;
  rateLimit: RateLimitSettingsDTO;
  mailgun: MailgunSettingsDTO;
}

interface MailgunSettingsDTO {
  maskedApiToken?: string;
}

interface RateLimitSettingsDTO {
  durationInMs: number;
  limit: number;
}

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
  sender: SenderDTO;
  receivers: ReceiverDTO[];
  subject: string;
  text: string;
  sentAt: string;
}

interface SenderDTO {
  mail: string;
}

interface ReceiverDTO {
  mail: string;
}

@Injectable()
export class MailingService {
  constructor(private readonly http: HttpClient) {}

  getSettings(): Observable<Settings> {
    return this.http.get<QuerySettingsResponse>(`${environment.apiUrl}/mailing/settings`).pipe(
      map((response) =>
        Settings.of({
          version: response.settings.version,
          rateLimit: RateLimitSettings.of({
            durationInMs: response.settings.rateLimit.durationInMs,
            limit: response.settings.rateLimit.limit,
          }),
          mailgun: MailgunSettings.of({
            apiToken: response.settings.mailgun.maskedApiToken,
          }),
        }),
      ),
    );
  }

  getMails(props: { skip?: number; limit?: number }): Observable<{ total: number; mails: Mail[] }> {
    const skip = someOrNone(props.skip).orElse(0);
    const limit = someOrNone(props.limit).orElse(100);

    let params = new HttpParams();
    params = params.set('skip', skip.toString());
    params = params.set('limit', limit.toString());

    return this.http.get<QueryMailsResponse>(`${environment.apiUrl}/mailing/mails`, { params }).pipe(
      map((response) => ({
        total: response.total,
        mails: response.mails.map((mail) => this.toInternalMail(mail)),
      })),
    );
  }

  getMail(id: string): Observable<Mail> {
    return this.http
      .get<QueryMailResponse>(`${environment.apiUrl}/mailing/mails/${id}`)
      .pipe(map((response) => this.toInternalMail(response.mail)));
  }

  updateRateLimit(version: number, durationInMs: number, limit: number): Observable<number> {
    const request: UpdateRateLimitRequest = {
      version,
      durationInMs,
      limit,
    };

    return this.http
      .post<UpdateRateLimitResponse>(`${environment.apiUrl}/mailing/settings/rate-limit/update`, request)
      .pipe(map((response) => response.version));
  }

  updateMailgunApiToken(version: number, apiToken: string): Observable<number> {
    const request: UpdateMailgunApiTokenRequest = {
      version,
      apiToken,
    };

    return this.http
      .post<UpdateMailgunApiTokenResponse>(`${environment.apiUrl}/mailing/settings/mailgun/api-token/update`, request)
      .pipe(map((response) => response.version));
  }

  clearMailgunApiToken(version: number): Observable<number> {
    const request: ClearMailgunApiTokenRequest = {
      version,
    };

    return this.http
      .post<ClearMailgunApiTokenResponse>(`${environment.apiUrl}/mailing/settings/mailgun/api-token/clear`, request)
      .pipe(map((response) => response.version));
  }

  private toInternalMail(mail: MailDTO): Mail {
    return Mail.of({
      id: mail.id,
      version: mail.version,
      sender: {
        mail: mail.sender.mail,
      },
      receivers: mail.receivers.map((receiver) => ({
        mail: receiver.mail,
      })),
      subject: mail.subject,
      text: mail.text,
      sentAt: new Date(mail.sentAt),
    });
  }
}
