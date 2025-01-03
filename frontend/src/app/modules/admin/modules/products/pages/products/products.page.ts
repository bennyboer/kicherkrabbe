import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, combineLatest, debounceTime, finalize, first, map, Subject, takeUntil } from 'rxjs';
import { Product } from '../../model';
import { none, Option, someOrNone } from '../../../../../shared/modules/option';
import { ProductsService } from '../../services';
import { NotificationService } from '../../../../../shared';
import { environment } from '../../../../../../../environments';

const PRODUCTS_LIMIT = 10;

@Component({
  selector: 'app-products-page',
  templateUrl: './products.page.html',
  styleUrls: ['./products.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class ProductsPage implements OnInit, OnDestroy {
  protected readonly productsLoaded$ = new BehaviorSubject<boolean>(false);
  protected readonly loadingProducts$ = new BehaviorSubject<boolean>(false);
  protected readonly products$ = new BehaviorSubject<Product[]>([]);
  protected readonly totalProducts$ = new BehaviorSubject<number>(0);

  protected readonly searchValue$ = new BehaviorSubject<string>('');

  protected readonly from$ = new BehaviorSubject<Option<Date>>(none());
  protected readonly to$ = new BehaviorSubject<Option<Date>>(none());

  protected readonly loading$ = this.loadingProducts$.asObservable();
  protected readonly remainingProductsCount$ = combineLatest([this.totalProducts$, this.products$]).pipe(
    map(([total, products]) => total - products.length),
  );
  protected readonly moreProductsAvailable$ = this.remainingProductsCount$.pipe(map((count) => count > 0));

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly productsService: ProductsService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    combineLatest([this.searchValue$.pipe(debounceTime(300)), this.from$, this.to$])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([searchValue, from, to]) =>
        this.reloadProducts({
          searchValue,
          from: from.orElseNull(),
          to: to.orElseNull(),
        }),
      );
  }

  ngOnDestroy(): void {
    this.productsLoaded$.complete();
    this.loadingProducts$.complete();
    this.products$.complete();

    this.searchValue$.complete();

    this.from$.complete();
    this.to$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  loadMoreProducts(): void {
    this.reloadProducts({
      searchValue: this.searchValue$.value,
      from: this.from$.value.orElseNull(),
      to: this.to$.value.orElseNull(),
      skip: this.products$.value.length,
      limit: PRODUCTS_LIMIT,
      keepAlreadyLoadedProducts: true,
    });
  }

  updateSearchTerm(value: string): void {
    this.searchValue$.next(value);
  }

  updateFrom(date: Date | null): void {
    this.from$.next(someOrNone(date));
  }

  updateTo(date: Date | null): void {
    this.to$.next(someOrNone(date));
  }

  private reloadProducts(props: {
    searchValue?: string;
    from?: Date | null;
    to?: Date | null;
    skip?: number;
    limit?: number;
    keepAlreadyLoadedProducts?: boolean;
  }): void {
    const searchValue = someOrNone(props.searchValue).orElse('');
    const from = someOrNone(props.from).orElseNull();
    const to = someOrNone(props.to).orElseNull();
    const skip = someOrNone(props.skip).orElse(0);
    const limit = someOrNone(props.limit).orElse(PRODUCTS_LIMIT);
    const keepAlreadyLoadedProducts = someOrNone(props.keepAlreadyLoadedProducts).orElse(false);

    if (this.loadingProducts$.value) {
      return;
    }
    this.loadingProducts$.next(true);

    this.productsService
      .getProducts({ searchValue, from, to, skip, limit })
      .pipe(
        first(),
        finalize(() => {
          this.loadingProducts$.next(false);
          this.productsLoaded$.next(true);
        }),
      )
      .subscribe({
        next: (page) => {
          this.totalProducts$.next(page.total);

          if (keepAlreadyLoadedProducts) {
            this.products$.next([...this.products$.value, ...page.products]);
          } else {
            this.products$.next(page.products);
          }
        },
        error: (e) => {
          console.error(e);

          this.notificationService.publish({
            message: 'Die Produkte konnten nicht geladen werden. Bitte versuchen Sie es erneut.',
            type: 'error',
          });
        },
      });
  }
}
