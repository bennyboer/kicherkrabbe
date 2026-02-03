import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, combineLatest, finalize, first, map, Observable, Subject, takeUntil } from 'rxjs';
import { none, Option, someOrNone } from '@kicherkrabbe/shared';
import { Notification } from '../../model';
import { NotificationsService } from '../../services';

const NOTIFICATIONS_LIMIT = 10;

@Component({
  selector: 'app-notifications-page',
  templateUrl: './notifications.page.html',
  styleUrls: ['./notifications.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class NotificationsPage implements OnInit, OnDestroy {
  protected readonly loadingNotifications$ = new BehaviorSubject<boolean>(false);
  protected readonly notifications$ = new BehaviorSubject<Notification[]>([]);
  protected readonly totalNotifications$ = new BehaviorSubject<number>(0);
  protected readonly notificationsLoaded$ = new BehaviorSubject<boolean>(false);
  protected readonly from$ = new BehaviorSubject<Option<Date>>(none());
  protected readonly to$ = new BehaviorSubject<Option<Date>>(none());

  protected readonly remainingNotificationsCount$: Observable<number> = combineLatest([
    this.notifications$,
    this.totalNotifications$,
  ]).pipe(map(([notifications, totalNotifications]) => totalNotifications - notifications.length));
  protected readonly moreNotificationsAvailable$: Observable<boolean> = this.remainingNotificationsCount$.pipe(
    map((remainingMailsCount) => remainingMailsCount > 0),
  );
  protected readonly loading$ = this.loadingNotifications$.asObservable();

  private readonly destroy$ = new Subject<void>();

  constructor(private readonly notificationsService: NotificationsService) {}

  ngOnInit(): void {
    combineLatest([this.from$, this.to$])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([from, to]) =>
        this.reloadNotifications({
          from: from.orElseNull(),
          to: to.orElseNull(),
        }),
      );
  }

  ngOnDestroy(): void {
    this.loadingNotifications$.complete();
    this.notifications$.complete();
    this.totalNotifications$.complete();
    this.notificationsLoaded$.complete();
    this.from$.complete();
    this.to$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateFrom(date?: Date | null): void {
    this.from$.next(someOrNone(date));
  }

  updateTo(date?: Date | null): void {
    this.to$.next(someOrNone(date));
  }

  loadMoreNotifications(): void {
    const skip = this.notifications$.value.length;
    const limit = NOTIFICATIONS_LIMIT;

    const from = this.from$.value.orElseNull();
    const to = this.to$.value.orElseNull();

    this.reloadNotifications({
      from,
      to,
      skip,
      limit,
      keepCurrentNotifications: true,
    });
  }

  private reloadNotifications(props: {
    from?: Date | null;
    to?: Date | null;
    skip?: number;
    limit?: number;
    keepCurrentNotifications?: boolean;
  }) {
    if (this.loadingNotifications$.value) {
      return;
    }
    this.loadingNotifications$.next(true);

    const from = someOrNone(props.from).orElseNull();
    const to = someOrNone(props.to).orElseNull();
    const skip = someOrNone(props.skip).orElse(0);
    const limit = someOrNone(props.limit).orElse(NOTIFICATIONS_LIMIT);
    const keepCurrentNotifications = someOrNone(props.keepCurrentNotifications).orElse(false);

    this.notificationsService
      .getNotifications({
        from,
        to,
        skip,
        limit,
      })
      .pipe(
        first(),
        finalize(() => {
          this.loadingNotifications$.next(false);
          this.notificationsLoaded$.next(true);
        }),
      )
      .subscribe((result) => {
        this.totalNotifications$.next(result.total);

        if (keepCurrentNotifications) {
          this.notifications$.next([...this.notifications$.value, ...result.notifications]);
        } else {
          this.notifications$.next(result.notifications);
        }
      });
  }
}
