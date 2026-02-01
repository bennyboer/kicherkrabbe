import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, combineLatest, delay, finalize, first, map, ReplaySubject, Subject, takeUntil } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationService } from '../../../../../shared';
import { ProductsService } from '../../services';
import { Product } from '../../model';

@Component({
  selector: 'app-delete-page',
  templateUrl: './delete.page.html',
  styleUrls: ['./delete.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class DeletePage implements OnInit, OnDestroy {
  protected readonly productId$ = new ReplaySubject<string>(1);
  protected readonly product$ = new ReplaySubject<Product>(1);
  protected readonly loadingProduct$ = new BehaviorSubject<boolean>(false);
  protected readonly deleting$ = new BehaviorSubject<boolean>(false);

  protected readonly loading$ = combineLatest([this.loadingProduct$, this.deleting$]).pipe(
    map(([loadingProduct, deleting]) => loadingProduct || deleting),
  );

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly productsService: ProductsService,
    private readonly notificationService: NotificationService,
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
    this.loadingProduct$.complete();
    this.deleting$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  deleteProduct(product: Product): void {
    if (this.deleting$.value) {
      return;
    }
    this.deleting$.next(true);

    this.productsService
      .deleteProduct(product.id, product.version)
      .pipe(
        delay(500),
        finalize(() => this.deleting$.next(false)),
      )
      .subscribe({
        next: () => {
          this.notificationService.publish({
            type: 'success',
            message: 'Die Produkt wurde gelöscht.',
          });
          this.router.navigate(['../..'], { relativeTo: this.route });
        },
        error: () => {
          this.notificationService.publish({
            type: 'error',
            message: 'Die Produkt konnte nicht gelöscht werden.',
          });
        },
      });
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
        finalize(() => this.loadingProduct$.next(false)),
      )
      .subscribe((product) => this.product$.next(product));
  }
}
