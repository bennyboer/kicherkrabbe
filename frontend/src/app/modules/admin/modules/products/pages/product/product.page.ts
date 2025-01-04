import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
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
import { none, Option, some } from '../../../../../shared/modules/option';
import { ActivatedRoute } from '@angular/router';
import { NotificationService } from '../../../../../shared';
import { Product } from '../../model';
import { ProductsService } from '../../services';
import { environment } from '../../../../../../../environments';
import { ImageSliderImage } from '../../../../../shared/modules/image-slider';
import { Theme, ThemeService } from '../../../../../../services';

@Component({
  selector: 'app-product-page',
  templateUrl: './product.page.html',
  styleUrls: ['./product.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class ProductPage implements OnInit, OnDestroy {
  private readonly productId$ = new ReplaySubject<string>(1);

  protected readonly product$ = new BehaviorSubject<Option<Product>>(none());
  private readonly loadingProduct$ = new BehaviorSubject<boolean>(false);
  protected readonly productLoaded$ = new BehaviorSubject<boolean>(false);

  protected readonly loading$ = this.loadingProduct$.asObservable();
  protected readonly images$: Observable<ImageSliderImage[]> = this.product$.pipe(
    map((product) => product.map((p) => this.toImageSliderImages(p.images)).orElse([])),
  );
  protected readonly theme$ = this.themeService
    .getTheme()
    .pipe(map((theme) => (theme === Theme.DARK ? 'dark' : 'light')));

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly productsService: ProductsService,
    private readonly notificationService: NotificationService,
    private readonly themeService: ThemeService,
  ) {}

  ngOnInit(): void {
    this.route.params
      .pipe(
        map((params) => params['productId']),
        takeUntil(this.destroy$),
      )
      .subscribe((productId) => this.productId$.next(productId));

    this.productId$.pipe(takeUntil(this.destroy$)).subscribe((productId) => this.reloadProduct(productId));
  }

  ngOnDestroy(): void {
    this.productId$.complete();

    this.product$.complete();
    this.productLoaded$.complete();
    this.loadingProduct$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  private toImageSliderImages(images: string[]): ImageSliderImage[] {
    return images.map((image) => ImageSliderImage.of({ url: this.getImageUrl(image) }));
  }

  private getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  private reloadProduct(productId: string): void {
    if (this.loadingProduct$.value) {
      return;
    }
    this.loadingProduct$.next(true);

    this.productsService
      .getProduct(productId)
      .pipe(
        first(),
        map((product) => some(product)),
        catchError((e) => {
          console.error('Failed to load product', e);
          this.notificationService.publish({
            message: 'Produkt konnte nicht geladen werden. Bitte versuche die Seite neu zu laden.',
            type: 'error',
          });

          return of(none<Product>());
        }),
        takeUntil(this.destroy$),
        finalize(() => {
          this.loadingProduct$.next(false);
          this.productLoaded$.next(true);
        }),
      )
      .subscribe((product) => this.product$.next(product));
  }
}
