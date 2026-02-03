import { ChangeDetectionStrategy, Component, Injector, OnDestroy, OnInit } from '@angular/core';
import {
  BehaviorSubject,
  catchError,
  distinctUntilChanged,
  finalize,
  first,
  map,
  Observable,
  of,
  ReplaySubject,
  Subject,
  takeUntil,
} from 'rxjs';
import { none, Option, some } from '@kicherkrabbe/shared';
import { ActivatedRoute } from '@angular/router';
import { ButtonSize, NotificationService } from '../../../../../shared';
import { Link, Product } from '../../model';
import { ProductsService } from '../../services';
import { environment } from '../../../../../../../environments';
import { ImageSliderImage } from '../../../../../shared/modules/image-slider';
import { Theme, ThemeService } from '../../../../../../services';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import {
  AddLinkDialog,
  AddLinkDialogData,
  AddLinkDialogResult,
  EditFabricCompositionDialog,
  EditFabricCompositionDialogData,
  EditFabricCompositionDialogResult,
  EditImagesDialog,
  EditImagesDialogData,
  EditImagesDialogResult,
  EditNoteDialog,
  EditNoteDialogData,
  EditNoteDialogResult,
  EditProducedAtDateDialog,
  EditProducedAtDateDialogData,
  EditProducedAtDateDialogResult,
  NoteType,
} from '../../dialogs';
import { AssetsService } from '../../../assets/services/assets.service';

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

  protected readonly removingLink$ = new BehaviorSubject<boolean>(false);

  protected readonly loading$ = this.loadingProduct$.asObservable();
  protected readonly images$: Observable<ImageSliderImage[]> = this.product$.pipe(
    map((product) => product.map((p) => this.toImageSliderImages(p.images)).orElse([])),
    distinctUntilChanged((a, b) => {
      if (a.length !== b.length) {
        return false;
      }

      return a.every((image, index) => image.equals(b[index]));
    }),
  );
  protected readonly theme$ = this.themeService
    .getTheme()
    .pipe(map((theme) => (theme === Theme.DARK ? 'dark' : 'light')));

  private readonly destroy$ = new Subject<void>();

  protected readonly ButtonSize = ButtonSize;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly productsService: ProductsService,
    private readonly assetsService: AssetsService,
    private readonly notificationService: NotificationService,
    private readonly themeService: ThemeService,
    private readonly dialogService: DialogService,
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

    this.removingLink$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  addLink(product: Product): void {
    const dialog = Dialog.create<AddLinkDialogResult>({
      title: 'Link hinzufügen',
      componentType: AddLinkDialog,
      injector: Injector.create({
        providers: [
          {
            provide: AddLinkDialogData,
            useValue: AddLinkDialogData.of({
              product: {
                id: product.id,
                version: product.version,
              },
              links: product.links,
            }),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog
        .getResult()
        .map((result) => product.addLink(result.version, result.link))
        .ifSome((updatedProduct) => this.product$.next(some(updatedProduct)));
    });
  }

  removeLink(event: Event, product: Product, link: Link): void {
    event.stopPropagation();
    event.preventDefault();

    if (this.removingLink$.value) {
      return;
    }
    this.removingLink$.next(true);

    this.productsService
      .removeLink({
        id: product.id,
        version: product.version,
        linkType: link.type,
        linkId: link.id,
      })
      .pipe(
        first(),
        finalize(() => this.removingLink$.next(false)),
      )
      .subscribe({
        next: (version) => {
          const updatedProduct = product.removeLink(version, link.type, link.id);
          this.product$.next(some(updatedProduct));

          this.notificationService.publish({
            message: 'Link wurde entfernt.',
            type: 'success',
          });
        },
        error: (e) => {
          console.error('Failed to remove link', e);
          this.notificationService.publish({
            message: 'Link konnte nicht entfernt werden. Bitte versuche es erneut.',
            type: 'error',
          });
        },
      });
  }

  editImages(product: Product): void {
    const dialog = Dialog.create<EditImagesDialogResult>({
      title: 'Bilder bearbeiten',
      componentType: EditImagesDialog,
      injector: Injector.create({
        providers: [
          {
            provide: EditImagesDialogData,
            useValue: EditImagesDialogData.of({
              product: {
                id: product.id,
                version: product.version,
              },
              images: product.images,
            }),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
          {
            provide: AssetsService,
            useValue: this.assetsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog
        .getResult()
        .map((result) => product.updateImages(result.version, result.images))
        .ifSome((updatedProduct) => this.product$.next(some(updatedProduct)));
    });
  }

  editFabricComposition(product: Product): void {
    const dialog = Dialog.create<EditFabricCompositionDialogResult>({
      title: 'Stoffzusammensetzung bearbeiten',
      componentType: EditFabricCompositionDialog,
      injector: Injector.create({
        providers: [
          {
            provide: EditFabricCompositionDialogData,
            useValue: EditFabricCompositionDialogData.of({
              product: {
                id: product.id,
                version: product.version,
              },
              fabricComposition: product.fabricComposition,
            }),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog
        .getResult()
        .map((result) => product.updateFabricComposition(result.version, result.fabricComposition))
        .ifSome((updatedProduct) => this.product$.next(some(updatedProduct)));
    });
  }

  editContainsNote(product: Product): void {
    const dialog = Dialog.create<EditNoteDialogResult>({
      title: 'Inhaltsangaben bearbeiten',
      componentType: EditNoteDialog,
      injector: Injector.create({
        providers: [
          {
            provide: EditNoteDialogData,
            useValue: EditNoteDialogData.of({
              product: {
                id: product.id,
                version: product.version,
              },
              notes: product.notes,
              noteType: NoteType.CONTAINS,
            }),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog
        .getResult()
        .map((result) => product.updateNotes(result.version, result.notes))
        .ifSome((updatedProduct) => this.product$.next(some(updatedProduct)));
    });
  }

  editCareNote(product: Product): void {
    const dialog = Dialog.create<EditNoteDialogResult>({
      title: 'Pflegehinweise bearbeiten',
      componentType: EditNoteDialog,
      injector: Injector.create({
        providers: [
          {
            provide: EditNoteDialogData,
            useValue: EditNoteDialogData.of({
              product: {
                id: product.id,
                version: product.version,
              },
              notes: product.notes,
              noteType: NoteType.CARE,
            }),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog
        .getResult()
        .map((result) => product.updateNotes(result.version, result.notes))
        .ifSome((updatedProduct) => this.product$.next(some(updatedProduct)));
    });
  }

  editSafetyNote(product: Product): void {
    const dialog = Dialog.create<EditNoteDialogResult>({
      title: 'Sicherheitshinweise bearbeiten',
      componentType: EditNoteDialog,
      injector: Injector.create({
        providers: [
          {
            provide: EditNoteDialogData,
            useValue: EditNoteDialogData.of({
              product: {
                id: product.id,
                version: product.version,
              },
              notes: product.notes,
              noteType: NoteType.SAFETY,
            }),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog
        .getResult()
        .map((result) => product.updateNotes(result.version, result.notes))
        .ifSome((updatedProduct) => this.product$.next(some(updatedProduct)));
    });
  }

  editProducedAtDate(product: Product): void {
    const dialog = Dialog.create<EditProducedAtDateDialogResult>({
      title: 'Produktionsdatum bearbeiten',
      componentType: EditProducedAtDateDialog,
      injector: Injector.create({
        providers: [
          {
            provide: EditProducedAtDateDialogData,
            useValue: EditProducedAtDateDialogData.of({
              product: {
                id: product.id,
                version: product.version,
              },
            }),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog
        .getResult()
        .map((result) => product.updateProducedAt(result.version, result.date))
        .ifSome((updatedProduct) => this.product$.next(some(updatedProduct)));
    });
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
