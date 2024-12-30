import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  debounceTime,
  distinctUntilChanged,
  finalize,
  first,
  map,
  Observable,
  of,
  Subject,
  takeUntil,
} from 'rxjs';
import { DropdownComponent, DropdownItem, DropdownItemId, NotificationService } from '../../../../../shared';
import { Mail, READ, Status, UNREAD } from '../../model';
import { MailboxService } from '../../services';
import { none, Option, some, someOrNone } from '../../../../../shared/modules/option';

const MAILS_LIMIT = 10;

@Component({
  selector: 'app-mailbox-page',
  templateUrl: './mailbox.page.html',
  styleUrls: ['./mailbox.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MailboxPage implements OnInit, OnDestroy {
  protected readonly searchTerm$: BehaviorSubject<string> = new BehaviorSubject<string>('');

  protected readonly mails$: BehaviorSubject<Mail[]> = new BehaviorSubject<Mail[]>([]);
  protected readonly totalMails$: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  private readonly loadingMails$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
  protected readonly mailsLoaded$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly remainingMailsCount$: Observable<number> = combineLatest([this.mails$, this.totalMails$]).pipe(
    map(([mails, totalMails]) => totalMails - mails.length),
  );
  protected readonly moreMailsAvailable$: Observable<boolean> = this.remainingMailsCount$.pipe(
    map((remainingMailsCount) => remainingMailsCount > 0),
  );

  protected readonly loading$: Observable<boolean> = this.loadingMails$.asObservable();

  protected readonly status$: BehaviorSubject<Option<Status>> = new BehaviorSubject<Option<Status>>(none());
  protected readonly statusDropdownItems$: BehaviorSubject<DropdownItem[]> = new BehaviorSubject<DropdownItem[]>([
    {
      id: 'read',
      label: 'Gelesen',
    },
    {
      id: 'unread',
      label: 'Ungelesen',
    },
  ]);

  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly mailboxService: MailboxService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    combineLatest([this.searchTerm$.pipe(distinctUntilChanged(), debounceTime(200)), this.status$])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([searchTerm, status]) => this.reloadMails({ searchTerm, status }));
  }

  ngOnDestroy(): void {
    this.searchTerm$.complete();
    this.statusDropdownItems$.complete();
    this.status$.complete();
    this.mails$.complete();
    this.totalMails$.complete();
    this.loadingMails$.complete();
    this.mailsLoaded$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateSearchTerm(value: string): void {
    this.searchTerm$.next(value);
  }

  updateSelectedStatus(dropdown: DropdownComponent, items: DropdownItemId[]): void {
    if (items.length === 0) {
      this.status$.next(none());
      return;
    }

    const status = items[0];
    if (status === 'read') {
      this.status$.next(some(READ));
    } else {
      this.status$.next(some(UNREAD));
    }

    dropdown.toggleOpened();
  }

  clearStatusSelection(dropdown: DropdownComponent): void {
    dropdown.clearSelection();
    dropdown.toggleOpened();
  }

  loadMoreMails(): void {
    const skip = this.mails$.value.length;
    const limit = MAILS_LIMIT;

    const searchTerm = this.searchTerm$.value.trim();
    const status = this.status$.value;

    this.reloadMails({
      searchTerm,
      status,
      skip,
      limit,
      keepCurrentMails: true,
    });
  }

  private reloadMails(props: {
    searchTerm: string;
    status: Option<Status>;
    skip?: number;
    limit?: number;
    keepCurrentMails?: boolean;
  }): void {
    const skip = someOrNone(props.skip).orElse(0);
    const limit = someOrNone(props.limit).orElse(MAILS_LIMIT);
    const keepCurrentMails = someOrNone(props.keepCurrentMails).orElse(false);

    this.loadingMails$.next(true);

    const searchTerm = props.searchTerm.trim();
    const status = props.status;

    this.mailboxService
      .getMails({
        searchTerm,
        status: status.orElseNull(),
        skip,
        limit,
      })
      .pipe(
        first(),
        catchError((_) => {
          this.notificationService.publish({
            type: 'error',
            message: 'Die Mails konnten nicht geladen werden. Bitte versuche die Seite neu zu laden.',
          });

          return of({
            total: 0,
            mails: [],
          });
        }),
        takeUntil(this.destroy$),
        finalize(() => {
          this.loadingMails$.next(false);
          this.mailsLoaded$.next(true);
        }),
      )
      .subscribe((result) => {
        this.totalMails$.next(result.total);

        if (keepCurrentMails) {
          this.mails$.next([...this.mails$.value, ...result.mails]);
        } else {
          this.mails$.next(result.mails);
        }
      });
  }
}
