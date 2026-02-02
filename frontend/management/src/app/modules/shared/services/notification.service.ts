import { Injectable, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { someOrNone } from '../modules/option';

export type NotificationType = 'info' | 'success' | 'error' | 'warn';

export class Notification {
  readonly message: string;
  readonly type: NotificationType;

  private constructor(props: { message: string; type: NotificationType }) {
    this.message = props.message;
    this.type = props.type;
  }

  static of(props: { message: string; type?: NotificationType }): Notification {
    return new Notification({
      message: props.message,
      type: someOrNone(props.type).orElse('info'),
    });
  }

  isError(): boolean {
    return this.type === 'error';
  }

  isSuccess(): boolean {
    return this.type === 'success';
  }

  isWarn(): boolean {
    return this.type === 'warn';
  }

  isInfo(): boolean {
    return this.type === 'info';
  }
}

@Injectable()
export class NotificationService implements OnDestroy {
  private readonly notifications$: Subject<Notification> = new Subject<Notification>();

  ngOnDestroy(): void {
    this.notifications$.complete();
  }

  publish(props: { message: string; type?: NotificationType }): void {
    const notification = Notification.of({
      message: props.message,
      type: props.type,
    });

    this.notifications$.next(notification);
  }

  getNotifications(): Observable<Notification> {
    return this.notifications$.asObservable();
  }
}
