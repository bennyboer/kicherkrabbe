import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, combineLatest, delay, finalize, first, map, ReplaySubject, Subject, takeUntil } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationService } from '../../../../../shared';
import { OffersService } from '../../services';
import { Offer } from '../../model';

@Component({
  selector: 'app-delete-page',
  templateUrl: './delete.page.html',
  styleUrls: ['./delete.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class DeletePage implements OnInit, OnDestroy {
  protected readonly offerId$ = new ReplaySubject<string>(1);
  protected readonly offer$ = new ReplaySubject<Offer>(1);
  protected readonly loadingOffer$ = new BehaviorSubject<boolean>(false);
  protected readonly deleting$ = new BehaviorSubject<boolean>(false);

  protected readonly loading$ = combineLatest([this.loadingOffer$, this.deleting$]).pipe(
    map(([loadingOffer, deleting]) => loadingOffer || deleting),
  );

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly offersService: OffersService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.route.params
      .pipe(
        map((params) => params['offerId']),
        takeUntil(this.destroy$),
      )
      .subscribe((offerId) => this.offerId$.next(offerId));

    this.offerId$.pipe(takeUntil(this.destroy$)).subscribe((offerId) => this.reloadOffer(offerId));
  }

  ngOnDestroy(): void {
    this.offerId$.complete();
    this.offer$.complete();
    this.loadingOffer$.complete();
    this.deleting$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  deleteOffer(offer: Offer): void {
    if (this.deleting$.value) {
      return;
    }
    this.deleting$.next(true);

    this.offersService
      .deleteOffer(offer.id, offer.version)
      .pipe(
        delay(500),
        finalize(() => this.deleting$.next(false)),
      )
      .subscribe({
        next: () => {
          this.notificationService.publish({
            type: 'success',
            message: 'Der Sofortkauf wurde gelöscht.',
          });
          this.router.navigate(['../..'], { relativeTo: this.route });
        },
        error: () => {
          this.notificationService.publish({
            type: 'error',
            message: 'Der Sofortkauf konnte nicht gelöscht werden.',
          });
        },
      });
  }

  private reloadOffer(offerId: string): void {
    if (this.loadingOffer$.value) {
      return;
    }
    this.loadingOffer$.next(true);

    this.offersService
      .getOffer(offerId)
      .pipe(
        first(),
        finalize(() => this.loadingOffer$.next(false)),
      )
      .subscribe((offer) => this.offer$.next(offer));
  }
}
