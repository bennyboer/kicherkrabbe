import { ChangeDetectionStrategy, Component } from '@angular/core';
import { BehaviorSubject, combineLatest, finalize, first, map, Observable, Subject } from 'rxjs';
import { someOrNone } from '../../../../../shared/modules/option';
import { MailingService } from '../../services';
import { Mail } from '../../model';

const MAILS_LIMIT = 10;

@Component({
  selector: 'app-mails-page',
  templateUrl: './mails.page.html',
  styleUrls: ['./mails.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class MailsPage {
  protected readonly loadingMails$ = new BehaviorSubject<boolean>(false);
  protected readonly mails$ = new BehaviorSubject<Mail[]>([]);
  protected readonly totalMails$ = new BehaviorSubject<number>(0);
  protected readonly mailsLoaded$ = new BehaviorSubject<boolean>(false);

  protected readonly remainingMailsCount$: Observable<number> = combineLatest([this.mails$, this.totalMails$]).pipe(
    map(([mails, totalMails]) => totalMails - mails.length),
  );
  protected readonly moreMailsAvailable$: Observable<boolean> = this.remainingMailsCount$.pipe(
    map((remainingMailsCount) => remainingMailsCount > 0),
  );
  protected readonly loading$ = this.loadingMails$.asObservable();

  private readonly destroy$ = new Subject<void>();

  constructor(private readonly mailingService: MailingService) {}

  ngOnInit(): void {
    this.reloadMails();
  }

  ngOnDestroy(): void {
    this.loadingMails$.complete();
    this.mails$.complete();
    this.totalMails$.complete();
    this.mailsLoaded$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  loadMoreMails(): void {
    const skip = this.mails$.value.length;
    const limit = MAILS_LIMIT;

    this.reloadMails({
      skip,
      limit,
      keepCurrentMails: true,
    });
  }

  private reloadMails(props?: { skip?: number; limit?: number; keepCurrentMails?: boolean }) {
    if (this.loadingMails$.value) {
      return;
    }
    this.loadingMails$.next(true);

    const skip = someOrNone(props?.skip).orElse(0);
    const limit = someOrNone(props?.limit).orElse(MAILS_LIMIT);
    const keepCurrentMails = someOrNone(props?.keepCurrentMails).orElse(false);

    this.mailingService
      .getMails({
        skip,
        limit,
      })
      .pipe(
        first(),
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
