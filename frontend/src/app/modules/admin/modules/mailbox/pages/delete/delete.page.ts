import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, Subject } from 'rxjs';
import { NotificationService } from '../../../../../shared';

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
    private readonly notificationService: NotificationService,
  ) {}

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  deleteMail(): void {
    this.deleting$.next(true);

    // TODO Delete mail in backend
    setTimeout(() => {
      this.notificationService.publish({
        type: 'success',
        message: 'Die Nachricht wurde gelöscht.',
      });
      this.router.navigate(['../..'], { relativeTo: this.route });
    }, 1000);
  }
}
