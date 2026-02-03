import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { Option, someOrNone } from '@kicherkrabbe/shared';
import { environment } from '../../environments';

interface QueryStatusResponse {
  enabled: boolean;
}

interface SendInquiryRequest {
  requestId: string;
  sender: SenderDTO;
  subject: string;
  message: string;
}

interface SenderDTO {
  name: string;
  mail: string;
  phone?: string;
}

export class InquiriesStatus {
  readonly enabled: boolean;

  private constructor(props: { enabled: boolean }) {
    this.enabled = props.enabled;
  }

  static of(props: { enabled?: boolean }): InquiriesStatus {
    return new InquiriesStatus({
      enabled: someOrNone(props.enabled).orElse(false),
    });
  }
}

export class Sender {
  readonly name: string;
  readonly mail: string;
  readonly phone: Option<string>;

  private constructor(props: { name: string; mail: string; phone: Option<string> }) {
    this.name = props.name;
    this.mail = props.mail;
    this.phone = props.phone;
  }

  static of(props: { name: string; mail: string; phone?: string | null }): Sender {
    return new Sender({
      name: props.name,
      mail: props.mail,
      phone: someOrNone(props.phone),
    });
  }
}

@Injectable({
  providedIn: 'root',
})
export class InquiriesService {
  constructor(private readonly http: HttpClient) {}

  getStatus(): Observable<InquiriesStatus> {
    return this.http
      .get<QueryStatusResponse>(`${environment.apiUrl}/inquiries/status`)
      .pipe(map((response) => InquiriesStatus.of({ enabled: response.enabled })));
  }

  send(props: { requestId: string; sender: Sender; subject: string; message: string }): Observable<void> {
    const request: SendInquiryRequest = {
      requestId: props.requestId,
      sender: {
        name: props.sender.name,
        mail: props.sender.mail,
      },
      subject: props.subject,
      message: props.message,
    };

    props.sender.phone.ifSome((phone) => {
      request.sender.phone = phone;
    });

    return this.http.post<void>(`${environment.apiUrl}/inquiries/send`, request);
  }
}
