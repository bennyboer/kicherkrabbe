import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { InquiriesStatus, Sender } from '../models';
import { environment } from '../../../../environments';

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

  send(props: {
    requestId: string;
    sender: Sender;
    subject: string;
    message: string;
  }): Observable<void> {
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

    return this.http.post<void>(
      `${environment.apiUrl}/inquiries/send`,
      request,
    );
  }
}
