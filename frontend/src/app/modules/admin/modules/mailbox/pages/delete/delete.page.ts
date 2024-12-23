import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, delay, finalize, Subject, switchMap } from 'rxjs';
import { NotificationService } from '../../../../../shared';
import { MailboxService } from '../../services';

@Component({
  selector: 'app-delete-page',
  templateUrl: './delete.page.html',
  styleUrls: ['./delete.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeletePage implements OnDestroy {
  protected readonly deleting$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly mailboxService: MailboxService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  deleteMail(): void {
    if (this.deleting$.value) {
      return;
    }
    this.deleting$.next(true);

    const mailId: string = this.route.snapshot.params['mailId'];

    this.mailboxService
      .getMail(mailId)
      .pipe(
        delay(500),
        switchMap((mail) =>
          this.mailboxService.deleteMail(mail.id, mail.version),
        ),
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
}
