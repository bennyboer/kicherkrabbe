import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, combineLatest, delay, finalize, first, map, ReplaySubject, Subject, takeUntil } from 'rxjs';
import { NotificationService } from '../../../../../shared';
import { MailboxService } from '../../services';
import { Mail } from '../../model';

@Component({
  selector: 'app-delete-page',
  templateUrl: './delete.page.html',
  styleUrls: ['./delete.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeletePage implements OnInit, OnDestroy {
  protected readonly mailId$ = new ReplaySubject<string>(1);
  protected readonly mail$ = new ReplaySubject<Mail>(1);
  protected readonly loadingMail$ = new BehaviorSubject<boolean>(false);
  protected readonly deleting$ = new BehaviorSubject<boolean>(false);

  protected readonly loading$ = combineLatest([this.loadingMail$, this.deleting$]).pipe(
    map(([loadingMail, deleting]) => loadingMail || deleting),
  );

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly router: Router,
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

    this.mailId$.pipe(takeUntil(this.destroy$)).subscribe((mailId) => this.reloadMail(mailId));
  }

  ngOnDestroy(): void {
    this.mailId$.complete();
    this.mail$.complete();
    this.loadingMail$.complete();
    this.deleting$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  deleteMail(mail: Mail): void {
    if (this.deleting$.value) {
      return;
    }
    this.deleting$.next(true);

    this.mailboxService
      .deleteMail(mail.id, mail.version)
      .pipe(
        delay(500),
        finalize(() => this.deleting$.next(false)),
      )
      .subscribe({
        next: () => {
          this.notificationService.publish({
            type: 'success',
            message: 'Die Mail wurde gelöscht.',
          });
          this.router.navigate(['../..'], { relativeTo: this.route });
        },
        error: () => {
          this.notificationService.publish({
            type: 'error',
            message: 'Die Mail konnte nicht gelöscht werden.',
          });
        },
      });
  }

  private reloadMail(mailId: string): void {
    if (this.loadingMail$.value) {
      return;
    }
    this.loadingMail$.next(true);

    this.mailboxService
      .getMail(mailId)
      .pipe(
        first(),
        finalize(() => this.loadingMail$.next(false)),
      )
      .subscribe((mail) => this.mail$.next(mail));
  }
}
