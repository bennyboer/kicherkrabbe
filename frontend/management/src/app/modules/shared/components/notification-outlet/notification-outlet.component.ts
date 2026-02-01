import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { Notification, NotificationService } from '../../services';
import { BehaviorSubject, map, Observable, Subject, Subscription, takeUntil, timer } from 'rxjs';
import { none, Option, some } from '../../modules/option';

@Component({
  selector: 'app-notification-outlet',
  templateUrl: './notification-outlet.component.html',
  styleUrls: ['./notification-outlet.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class NotificationOutletComponent implements OnInit, OnDestroy {
  private readonly notificationToShow$: BehaviorSubject<Option<Notification>> = new BehaviorSubject<
    Option<Notification>
  >(none());
  private readonly destroy$: Subject<void> = new Subject<void>();

  private timerSub: Option<Subscription> = none();

  constructor(private readonly notificationService: NotificationService) {}

  ngOnInit(): void {
    this.notificationService
      .getNotifications()
      .pipe(takeUntil(this.destroy$))
      .subscribe((notification) => {
        this.timerSub.ifSome((sub) => sub.unsubscribe());

        this.notificationToShow$.next(some(notification));

        this.timerSub = some(
          timer(5000)
            .pipe(takeUntil(this.destroy$))
            .subscribe(() => this.notificationToShow$.next(none())),
        );
      });
  }

  ngOnDestroy(): void {
    this.notificationToShow$.complete();

    this.timerSub.ifSome((sub) => sub.unsubscribe());

    this.destroy$.next();
    this.destroy$.complete();
  }

  getNotificationToShow(): Observable<Notification | null> {
    return this.notificationToShow$.asObservable().pipe(map((o) => o.unwrap()));
  }
}
