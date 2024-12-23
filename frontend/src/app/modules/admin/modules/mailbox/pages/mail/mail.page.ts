import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  BehaviorSubject,
  catchError,
  finalize,
  first,
  map,
  Observable,
  of,
  ReplaySubject,
  Subject,
  takeUntil,
} from 'rxjs';
import { Mail } from '../../model';
import { none, Option, some } from '../../../../../shared/modules/option';
import { MailboxService } from '../../services';
import { NotificationService } from '../../../../../shared';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-mail-page',
  templateUrl: './mail.page.html',
  styleUrls: ['./mail.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MailPage implements OnInit, OnDestroy {
  private readonly mailId$: ReplaySubject<string> = new ReplaySubject<string>(
    1,
  );
  protected readonly mail$: BehaviorSubject<Option<Mail>> = new BehaviorSubject<
    Option<Mail>
  >(none());
  private readonly loadingMail$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);
  protected readonly mailLoaded$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  protected readonly loading$: Observable<boolean> =
    this.loadingMail$.asObservable();

  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly mailboxService: MailboxService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.route.params
      .pipe(
        map((params) => params['mailId']),
        takeUntil(this.destroy$),
      )
      .subscribe((mailId) => this.mailId$.next(mailId));

    this.mailId$
      .pipe(takeUntil(this.destroy$))
      .subscribe((mailId) => this.reloadMail(mailId));
  }

  ngOnDestroy(): void {
    this.mailId$.complete();
    this.mail$.complete();
    this.mailLoaded$.complete();
    this.loadingMail$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  private reloadMail(mailId: string): void {
    this.loadingMail$.next(true);

    this.mailboxService
      .getMail(mailId)
      .pipe(
        first(),
        map((mail) => some(mail)),
        catchError((e) => {
          console.error('Failed to load mail', e);
          this.notificationService.publish({
            message:
              'Mail konnte nicht geladen werden. Bitte versuche die Seite neu zu laden.',
            type: 'error',
          });

          return of(none<Mail>());
        }),
        takeUntil(this.destroy$),
        finalize(() => {
          this.loadingMail$.next(false);
          this.mailLoaded$.next(true);
        }),
      )
      .subscribe((mail) => this.mail$.next(mail));
  }
}
