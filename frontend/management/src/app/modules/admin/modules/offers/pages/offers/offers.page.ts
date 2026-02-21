import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, combineLatest, debounceTime, finalize, first, map, Subject, takeUntil } from 'rxjs';
import { Offer } from '../../model';
import { someOrNone } from '@kicherkrabbe/shared';
import { OffersService } from '../../services';
import { NotificationService } from '../../../../../shared';
import { environment } from '../../../../../../../environments';

const OFFERS_LIMIT = 10;

@Component({
  selector: 'app-offers-page',
  templateUrl: './offers.page.html',
  styleUrls: ['./offers.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class OffersPage implements OnInit, OnDestroy {
  protected readonly offersLoaded$ = new BehaviorSubject<boolean>(false);
  protected readonly loadingOffers$ = new BehaviorSubject<boolean>(false);
  protected readonly offers$ = new BehaviorSubject<Offer[]>([]);
  protected readonly totalOffers$ = new BehaviorSubject<number>(0);

  protected readonly searchValue$ = new BehaviorSubject<string>('');

  protected readonly loading$ = this.loadingOffers$.asObservable();
  protected readonly remainingOffersCount$ = combineLatest([this.totalOffers$, this.offers$]).pipe(
    map(([total, offers]) => total - offers.length),
  );
  protected readonly moreOffersAvailable$ = this.remainingOffersCount$.pipe(map((count) => count > 0));

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly offersService: OffersService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.searchValue$
      .pipe(debounceTime(300), takeUntil(this.destroy$))
      .subscribe((searchValue) => this.reloadOffers({ searchTerm: searchValue }));
  }

  ngOnDestroy(): void {
    this.offersLoaded$.complete();
    this.loadingOffers$.complete();
    this.offers$.complete();
    this.totalOffers$.complete();

    this.searchValue$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  loadMoreOffers(): void {
    this.reloadOffers({
      searchTerm: this.searchValue$.value,
      skip: this.offers$.value.length,
      limit: OFFERS_LIMIT,
      keepAlreadyLoadedOffers: true,
    });
  }

  updateSearchTerm(value: string): void {
    this.searchValue$.next(value);
  }

  private reloadOffers(props: {
    searchTerm?: string;
    skip?: number;
    limit?: number;
    keepAlreadyLoadedOffers?: boolean;
  }): void {
    const searchTerm = someOrNone(props.searchTerm).orElse('');
    const skip = someOrNone(props.skip).orElse(0);
    const limit = someOrNone(props.limit).orElse(OFFERS_LIMIT);
    const keepAlreadyLoadedOffers = someOrNone(props.keepAlreadyLoadedOffers).orElse(false);

    if (this.loadingOffers$.value) {
      return;
    }
    this.loadingOffers$.next(true);

    this.offersService
      .getOffers({ searchTerm, skip, limit })
      .pipe(
        first(),
        finalize(() => {
          this.loadingOffers$.next(false);
          this.offersLoaded$.next(true);
        }),
      )
      .subscribe({
        next: (page) => {
          this.totalOffers$.next(page.total);

          if (keepAlreadyLoadedOffers) {
            this.offers$.next([...this.offers$.value, ...page.offers]);
          } else {
            this.offers$.next(page.offers);
          }
        },
        error: (e) => {
          console.error(e);

          this.notificationService.publish({
            message: 'Die Sofortkäufe konnten nicht geladen werden. Bitte versuchen Sie es erneut.',
            type: 'error',
          });
        },
      });
  }
}
