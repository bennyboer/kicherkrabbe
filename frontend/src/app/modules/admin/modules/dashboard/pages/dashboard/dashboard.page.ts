import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MailboxService } from '../../services';
import { BehaviorSubject, finalize, first, map } from 'rxjs';

@Component({
    selector: 'app-dashboard-page',
    templateUrl: './dashboard.page.html',
    styleUrls: ['./dashboard.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class DashboardPage implements OnInit, OnDestroy {
  protected readonly unreadMailsCount$ = new BehaviorSubject<number>(0);
  protected readonly loadingUnreadMailsCount$ = new BehaviorSubject<boolean>(false);
  protected readonly unreadMailsCountLoaded$ = this.loadingUnreadMailsCount$.pipe(map((loading) => !loading));

  protected readonly loading$ = this.loadingUnreadMailsCount$.asObservable();

  constructor(private readonly mailboxService: MailboxService) {}

  ngOnInit(): void {
    this.reloadUnreadMailsCount();
  }

  ngOnDestroy(): void {
    this.unreadMailsCount$.complete();
    this.loadingUnreadMailsCount$.complete();
  }

  private reloadUnreadMailsCount(): void {
    if (this.loadingUnreadMailsCount$.value) {
      return;
    }
    this.loadingUnreadMailsCount$.next(true);

    this.mailboxService
      .getUnreadMailsCount()
      .pipe(
        first(),
        finalize(() => this.loadingUnreadMailsCount$.next(false)),
      )
      .subscribe((count) => this.unreadMailsCount$.next(count));
  }
}
