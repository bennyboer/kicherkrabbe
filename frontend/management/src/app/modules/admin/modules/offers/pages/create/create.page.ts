import { ChangeDetectionStrategy, Component, EnvironmentInjector, OnDestroy, OnInit } from '@angular/core';
import { ButtonSize, Chip, NotificationService } from '../../../../../shared';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  debounceTime,
  delay,
  distinctUntilChanged,
  finalize,
  first,
  map,
  Subject,
  takeUntil,
} from 'rxjs';
import { Theme, ThemeService } from '../../../../../../services';
import { Money, Notes, OfferCategory } from '../../model';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import {
  EditImagesDialog,
  EditImagesDialogData,
  EditImagesDialogResult,
  EditNoteDialog,
  EditNoteDialogData,
  EditNoteDialogResult,
  EditPriceDialog,
  EditPriceDialogData,
  EditPriceDialogResult,
  NoteType,
} from '../../dialogs';
import { OfferCategoriesService, OffersService, ProductForOfferCreation } from '../../services';
import { none, Option, some, someOrNone } from '@kicherkrabbe/shared';
import { AssetsService } from '../../../assets/services/assets.service';
import { ImageSliderImage } from '../../../../../shared/modules/image-slider';
import { environment } from '../../../../../../../environments';
import { ActivatedRoute, Router } from '@angular/router';

const PRODUCTS_LIMIT = 10;

@Component({
  selector: 'app-create-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class CreatePage implements OnInit, OnDestroy {
  protected readonly selectedProduct$ = new BehaviorSubject<Option<ProductForOfferCreation>>(none());
  protected readonly productSearchValue$ = new BehaviorSubject<string>('');
  protected readonly products$ = new BehaviorSubject<ProductForOfferCreation[]>([]);
  protected readonly totalProducts$ = new BehaviorSubject<number>(0);
  protected readonly loadingProducts$ = new BehaviorSubject<boolean>(false);

  protected readonly title$ = new BehaviorSubject<string>('');
  protected readonly size$ = new BehaviorSubject<string>('');
  protected readonly availableCategories$ = new BehaviorSubject<OfferCategory[]>([]);
  protected readonly loadingCategories$ = new BehaviorSubject<boolean>(true);
  protected readonly selectedCategories$ = new BehaviorSubject<OfferCategory[]>([]);

  protected readonly images$ = new BehaviorSubject<string[]>([]);
  protected readonly notes$ = new BehaviorSubject<Notes>(Notes.empty());
  protected readonly price$ = new BehaviorSubject<Option<Money>>(none());

  protected readonly creating$ = new BehaviorSubject<boolean>(false);

  protected readonly theme$ = this.themeService
    .getTheme()
    .pipe(map((theme) => (theme === Theme.DARK ? 'dark' : 'light')));
  protected readonly priceMissing$ = this.price$.pipe(map((price) => price.isNone()));
  protected readonly productMissing$ = this.selectedProduct$.pipe(map((product) => product.isNone()));
  protected readonly titleMissing$ = this.title$.pipe(map((title) => title.trim().length === 0));
  protected readonly sizeMissing$ = this.size$.pipe(map((size) => size.trim().length === 0));
  protected readonly descriptionMissing$ = this.notes$.pipe(map((notes) => notes.description.length === 0));
  protected readonly invalid$ = combineLatest([this.priceMissing$, this.productMissing$, this.titleMissing$, this.sizeMissing$, this.descriptionMissing$]).pipe(
    map(([priceMissing, productMissing, titleMissing, sizeMissing, descriptionMissing]) => priceMissing || productMissing || titleMissing || sizeMissing || descriptionMissing),
  );
  protected readonly cannotCreate$ = combineLatest([this.creating$, this.invalid$]).pipe(
    map(([creating, invalid]) => creating || invalid),
  );
  protected readonly imageSliderImages$ = this.images$.pipe(
    map((images) => this.toImageSliderImages(images)),
    distinctUntilChanged((a, b) => {
      if (a.length !== b.length) {
        return false;
      }

      return a.every((image, index) => image.equals(b[index]));
    }),
  );
  protected readonly remainingProductsCount$ = combineLatest([this.totalProducts$, this.products$]).pipe(
    map(([total, products]) => total - products.length),
  );
  protected readonly moreProductsAvailable$ = this.remainingProductsCount$.pipe(map((count) => count > 0));

  private readonly destroy$ = new Subject<void>();

  protected readonly ButtonSize = ButtonSize;

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly themeService: ThemeService,
    private readonly dialogService: DialogService,
    private readonly offersService: OffersService,
    private readonly offerCategoriesService: OfferCategoriesService,
    private readonly assetsService: AssetsService,
    private readonly notificationService: NotificationService,
    private readonly environmentInjector: EnvironmentInjector,
  ) {}

  ngOnInit(): void {
    this.reloadAvailableCategories();

    this.productSearchValue$
      .pipe(debounceTime(300), takeUntil(this.destroy$))
      .subscribe((searchValue) => this.reloadProducts({ searchTerm: searchValue }));
  }

  ngOnDestroy(): void {
    this.selectedProduct$.complete();
    this.productSearchValue$.complete();
    this.products$.complete();
    this.totalProducts$.complete();
    this.loadingProducts$.complete();
    this.title$.complete();
    this.size$.complete();
    this.availableCategories$.complete();
    this.loadingCategories$.complete();
    this.selectedCategories$.complete();
    this.images$.complete();
    this.notes$.complete();
    this.price$.complete();
    this.creating$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateProductSearchTerm(value: string): void {
    this.productSearchValue$.next(value);
  }

  selectProduct(product: ProductForOfferCreation): void {
    this.selectedProduct$.next(some(product));
    this.images$.next(product.imageIds);
  }

  changeProduct(): void {
    this.selectedProduct$.next(none());
    this.title$.next('');
    this.size$.next('');
    this.selectedCategories$.next([]);
    this.images$.next([]);
    this.notes$.next(Notes.empty());
    this.price$.next(none());
  }

  loadMoreProducts(): void {
    this.reloadProducts({
      searchTerm: this.productSearchValue$.value,
      skip: this.products$.value.length,
      limit: PRODUCTS_LIMIT,
      keepAlreadyLoaded: true,
    });
  }

  updateTitle(value: string): void {
    this.title$.next(value);
  }

  updateSize(value: string): void {
    this.size$.next(value);
  }

  categoriesToChips(categories: OfferCategory[]): Chip[] {
    return categories.map((category) => Chip.of({ id: category.id, label: category.name }));
  }

  onCategoryRemoved(chip: Chip): void {
    const categories = this.selectedCategories$.value.filter((category) => category.id !== chip.id);
    this.selectedCategories$.next(categories);
  }

  onCategoryAdded(chip: Chip): void {
    const category = this.availableCategories$.value.find((c) => c.id === chip.id);
    if (category) {
      const categories = [...this.selectedCategories$.value, category];
      this.selectedCategories$.next(categories);
    }
  }

  create(): void {
    if (this.creating$.value) {
      return;
    }

    const product = this.selectedProduct$.value;
    const price = this.price$.value;
    const title = this.title$.value.trim();
    const size = this.size$.value.trim();

    const notes = this.notes$.value;

    if (product.isNone() || price.isNone() || title.length === 0 || size.length === 0 || notes.description.length === 0) {
      return;
    }

    this.creating$.next(true);

    this.offersService
      .createOffer({
        title,
        size,
        categoryIds: this.selectedCategories$.value.map((c) => c.id),
        productId: product.orElseThrow().id,
        imageIds: this.images$.value,
        notes: this.notes$.value,
        price: price.orElseThrow(),
      })
      .pipe(
        first(),
        delay(500),
        finalize(() => this.creating$.next(false)),
      )
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: 'Sofortkauf wurde erfolgreich erstellt.',
            type: 'success',
          });

          this.router.navigate(['..'], { relativeTo: this.route });
        },
        error: (e) => {
          console.error('Failed to create offer', e);
          this.notificationService.publish({
            message: 'Sofortkauf konnte nicht erstellt werden. Bitte versuchen Sie es erneut.',
            type: 'error',
          });
        },
      });
  }

  editImages(images: string[]): void {
    const dialog = Dialog.create<EditImagesDialogResult>({
      title: 'Bilder bearbeiten',
      componentType: EditImagesDialog,
      providers: [
        {
          provide: EditImagesDialogData,
          useValue: EditImagesDialogData.of({
            images,
          }),
        },
        {
          provide: OffersService,
          useValue: this.offersService,
        },
        {
          provide: AssetsService,
          useValue: this.assetsService,
        },
      ],
      environmentInjector: this.environmentInjector,
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog.getResult().ifSome((result) => this.images$.next(result.images));
    });
  }

  editDescriptionNote(notes: Notes): void {
    this.editNote(notes, NoteType.DESCRIPTION, 'Beschreibung bearbeiten');
  }

  editContainsNote(notes: Notes): void {
    this.editNote(notes, NoteType.CONTAINS, 'Inhaltsangaben bearbeiten');
  }

  editCareNote(notes: Notes): void {
    this.editNote(notes, NoteType.CARE, 'Pflegehinweise bearbeiten');
  }

  editSafetyNote(notes: Notes): void {
    this.editNote(notes, NoteType.SAFETY, 'Sicherheitshinweise bearbeiten');
  }

  editPrice(price: Option<Money>): void {
    const dialog = Dialog.create<EditPriceDialogResult>({
      title: 'Preis festlegen',
      componentType: EditPriceDialog,
      providers: [
        {
          provide: EditPriceDialogData,
          useValue: EditPriceDialogData.of({
            currentPrice: price.orElse(undefined!),
          }),
        },
        {
          provide: OffersService,
          useValue: this.offersService,
        },
      ],
      environmentInjector: this.environmentInjector,
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog.getResult().ifSome((result) => this.price$.next(some(result.price)));
    });
  }

  private editNote(notes: Notes, noteType: NoteType, title: string): void {
    const dialog = Dialog.create<EditNoteDialogResult>({
      title,
      componentType: EditNoteDialog,
      providers: [
        {
          provide: EditNoteDialogData,
          useValue: EditNoteDialogData.of({
            notes,
            noteType,
          }),
        },
        {
          provide: OffersService,
          useValue: this.offersService,
        },
      ],
      environmentInjector: this.environmentInjector,
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog.getResult().ifSome((result) => this.notes$.next(result.notes));
    });
  }

  private toImageSliderImages(images: string[]): ImageSliderImage[] {
    return images.map((image) => ImageSliderImage.of({ url: this.getImageUrl(image) }));
  }

  private getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  private reloadAvailableCategories(): void {
    this.loadingCategories$.next(true);

    this.offerCategoriesService
      .getAvailableCategories()
      .pipe(
        first(),
        catchError((e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Kategorien konnten nicht geladen werden. Bitte versuche die Seite neuzuladen.',
          });
          return [];
        }),
        finalize(() => this.loadingCategories$.next(false)),
      )
      .subscribe((categories) => this.availableCategories$.next(categories));
  }

  private reloadProducts(props: {
    searchTerm?: string;
    skip?: number;
    limit?: number;
    keepAlreadyLoaded?: boolean;
  }): void {
    const searchTerm = someOrNone(props.searchTerm).orElse('');
    const skip = someOrNone(props.skip).orElse(0);
    const limit = someOrNone(props.limit).orElse(PRODUCTS_LIMIT);
    const keepAlreadyLoaded = someOrNone(props.keepAlreadyLoaded).orElse(false);

    if (this.loadingProducts$.value) {
      return;
    }
    this.loadingProducts$.next(true);

    this.offersService
      .getProductsForOfferCreation({ searchTerm, skip, limit })
      .pipe(
        first(),
        finalize(() => this.loadingProducts$.next(false)),
      )
      .subscribe({
        next: (page) => {
          this.totalProducts$.next(page.total);

          if (keepAlreadyLoaded) {
            this.products$.next([...this.products$.value, ...page.products]);
          } else {
            this.products$.next(page.products);
          }
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            message: 'Produkte konnten nicht geladen werden.',
            type: 'error',
          });
        },
      });
  }
}
