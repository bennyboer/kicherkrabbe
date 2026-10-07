import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, combineLatest, delay, finalize, first, map, ReplaySubject, Subject, takeUntil } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationService } from '../../../../../shared';
import { EmbroideriesService } from '../../services';
import { Embroidery } from '../../model';

@Component({
  selector: 'app-delete-embroidery-page',
  templateUrl: './delete.page.html',
  styleUrls: ['./delete.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class DeletePage implements OnInit, OnDestroy {
  protected readonly embroideryId$ = new ReplaySubject<string>(1);
  protected readonly embroidery$ = new ReplaySubject<Embroidery>(1);
  protected readonly loadingEmbroidery$ = new BehaviorSubject<boolean>(false);
  protected readonly deleting$ = new BehaviorSubject<boolean>(false);

  protected readonly loading$ = combineLatest([this.loadingEmbroidery$, this.deleting$]).pipe(
    map(([loadingEmbroidery, deleting]) => loadingEmbroidery || deleting),
  );

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly embroideriesService: EmbroideriesService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.route.params
      .pipe(
        map((params) => params['embroideryId']),
        takeUntil(this.destroy$),
      )
      .subscribe((embroideryId) => this.embroideryId$.next(embroideryId));

    this.embroideryId$.pipe(takeUntil(this.destroy$)).subscribe((embroideryId) => this.reloadEmbroidery(embroideryId));
  }

  ngOnDestroy(): void {
    this.embroideryId$.complete();
    this.embroidery$.complete();
    this.loadingEmbroidery$.complete();
    this.deleting$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  deleteEmbroidery(embroidery: Embroidery): void {
    if (this.deleting$.value) {
      return;
    }
    this.deleting$.next(true);

    this.embroideriesService
      .deleteEmbroidery(embroidery.id, embroidery.version)
      .pipe(
        delay(500),
        finalize(() => this.deleting$.next(false)),
      )
      .subscribe({
        next: () => {
          this.notificationService.publish({
            type: 'success',
            message: 'Die Stickerei wurde gelöscht.',
          });
          this.router.navigate(['../..'], { relativeTo: this.route });
        },
        error: () => {
          this.notificationService.publish({
            type: 'error',
            message: 'Die Stickerei konnte nicht gelöscht werden.',
          });
        },
      });
  }

  private reloadEmbroidery(embroideryId: string): void {
    if (this.loadingEmbroidery$.value) {
      return;
    }
    this.loadingEmbroidery$.next(true);

    this.embroideriesService
      .getEmbroidery(embroideryId)
      .pipe(
        first(),
        finalize(() => this.loadingEmbroidery$.next(false)),
      )
      .subscribe((embroidery) => this.embroidery$.next(embroidery));
  }
}
