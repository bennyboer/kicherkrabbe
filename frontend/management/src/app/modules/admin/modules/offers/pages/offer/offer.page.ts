import { ChangeDetectionStrategy, Component, EnvironmentInjector, OnDestroy, OnInit } from '@angular/core';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  debounceTime,
  distinctUntilChanged,
  filter,
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
import { ButtonSize, Chip, NotificationService } from '../../../../../shared';
import { Offer, OfferCategory, OfferCategoryId, OfferStatus } from '../../model';
import { OfferCategoriesService, OffersService } from '../../services';
import { environment } from '../../../../../../../environments';
import { ImageSliderImage } from '../../../../../shared/modules/image-slider';
import { Theme, ThemeService } from '../../../../../../services';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import {
  AddDiscountDialog,
  AddDiscountDialogData,
  AddDiscountDialogResult,
  ConfirmArchiveDialog,
  ConfirmArchiveDialogResult,
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
import { AssetsService } from '../../../assets/services/assets.service';

@Component({
  selector: 'app-offer-page',
  templateUrl: './offer.page.html',
  styleUrls: ['./offer.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class OfferPage implements OnInit, OnDestroy {
  private readonly offerId$ = new ReplaySubject<string>(1);

  protected readonly offer$ = new BehaviorSubject<Option<Offer>>(none());
  private readonly loadingOffer$ = new BehaviorSubject<boolean>(false);
  protected readonly offerLoaded$ = new BehaviorSubject<boolean>(false);
  protected readonly actionInProgress$ = new BehaviorSubject<boolean>(false);

  protected readonly title$ = new BehaviorSubject<string>('');
  private readonly titleValid$ = this.title$.pipe(map((title) => title.trim().length > 0));
  private readonly titleChanged$ = combineLatest([this.offer$, this.title$]).pipe(
    map(([offer, title]) => offer.map((o) => o.title !== title).orElse(false)),
  );
  protected readonly cannotSaveTitle$ = combineLatest([this.titleValid$, this.titleChanged$]).pipe(
    map(([valid, changed]) => !valid || !changed),
  );

  protected readonly size$ = new BehaviorSubject<string>('');
  private readonly sizeValid$ = this.size$.pipe(map((size) => size.trim().length > 0));
  private readonly sizeChanged$ = combineLatest([this.offer$, this.size$]).pipe(
    map(([offer, size]) => offer.map((o) => o.size !== size).orElse(false)),
  );
  protected readonly cannotSaveSize$ = combineLatest([this.sizeValid$, this.sizeChanged$]).pipe(
    map(([valid, changed]) => !valid || !changed),
  );

  protected readonly availableCategories$ = new BehaviorSubject<OfferCategory[]>([]);
  protected readonly loadingCategories$ = new BehaviorSubject<boolean>(true);
  protected readonly selectedCategories$ = new BehaviorSubject<OfferCategoryId[]>([]);

  protected readonly loading$ = combineLatest([this.loadingOffer$, this.loadingCategories$]).pipe(
    map(([offerLoading, categoriesLoading]) => offerLoading || categoriesLoading),
  );
  protected readonly images$: Observable<ImageSliderImage[]> = this.offer$.pipe(
    map((offer) => offer.map((o) => this.toImageSliderImages(o.images)).orElse([])),
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
  protected readonly OfferStatus = OfferStatus;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly offersService: OffersService,
    private readonly offerCategoriesService: OfferCategoriesService,
    private readonly assetsService: AssetsService,
    private readonly notificationService: NotificationService,
    private readonly themeService: ThemeService,
    private readonly dialogService: DialogService,
    private readonly environmentInjector: EnvironmentInjector,
  ) {}

  ngOnInit(): void {
    this.reloadAvailableCategories();

    this.route.params
      .pipe(
        map((params) => params['offerId']),
        takeUntil(this.destroy$),
      )
      .subscribe((offerId) => this.offerId$.next(offerId));

    this.offerId$.pipe(takeUntil(this.destroy$)).subscribe((offerId) => this.reloadOffer(offerId));

    combineLatest([this.offer$, this.selectedCategories$.pipe(debounceTime(300))])
      .pipe(
        filter(([offer, categories]) => {
          if (offer.isNone()) {
            return false;
          }
          const o = offer.orElseThrow();
          return !this.areSetsEqual(o.categories, new Set<OfferCategoryId>(categories));
        }),
        takeUntil(this.destroy$),
      )
      .subscribe(([offer, categories]) => this.saveUpdatedCategories(offer.orElseThrow(), categories));
  }

  ngOnDestroy(): void {
    this.offerId$.complete();

    this.offer$.complete();
    this.offerLoaded$.complete();
    this.loadingOffer$.complete();
    this.actionInProgress$.complete();
    this.title$.complete();
    this.size$.complete();
    this.availableCategories$.complete();
    this.loadingCategories$.complete();
    this.selectedCategories$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateTitle(value: string): void {
    this.title$.next(value);
  }

  saveTitle(offer: Offer): void {
    const title = this.title$.value.trim();
    if (title.length === 0) {
      return;
    }

    this.offersService
      .updateTitle({ id: offer.id, version: offer.version, title })
      .pipe(first())
      .subscribe({
        next: (version) => {
          const updatedOffer = offer.updateTitle(version, title);
          this.offer$.next(some(updatedOffer));

          this.notificationService.publish({
            message: 'Titel wurde aktualisiert.',
            type: 'success',
          });
        },
        error: (e) => {
          const reason = e?.error?.reason;
          if (reason === 'ALIAS_ALREADY_IN_USE') {
            this.notificationService.publish({
              type: 'error',
              message: 'Es existiert bereits ein Sofortkauf mit diesem Titel.',
            });
          } else {
            console.error('Failed to update title', e);
            this.notificationService.publish({
              message: 'Titel konnte nicht aktualisiert werden. Bitte versuche es erneut.',
              type: 'error',
            });
          }
        },
      });
  }

  updateSize(value: string): void {
    this.size$.next(value);
  }

  saveSize(offer: Offer): void {
    const size = this.size$.value.trim();
    if (size.length === 0) {
      return;
    }

    this.offersService
      .updateSize({ id: offer.id, version: offer.version, size })
      .pipe(first())
      .subscribe({
        next: (version) => {
          const updatedOffer = offer.updateSize(version, size);
          this.offer$.next(some(updatedOffer));

          this.notificationService.publish({
            message: 'Größe wurde aktualisiert.',
            type: 'success',
          });
        },
        error: (e) => {
          console.error('Failed to update size', e);
          this.notificationService.publish({
            message: 'Größe konnte nicht aktualisiert werden. Bitte versuche es erneut.',
            type: 'error',
          });
        },
      });
  }

  toCategories(ids: OfferCategoryId[], categories: OfferCategory[]): OfferCategory[] {
    const lookup = new Map<OfferCategoryId, OfferCategory>();
    for (const category of categories) {
      lookup.set(category.id, category);
    }

    const result = ids.map((id) => lookup.get(id)).filter((category) => !!category);

    result.sort((a, b) =>
      a.name.localeCompare(b.name, 'de-de', {
        numeric: true,
      }),
    );

    return result;
  }

  categoriesToChips(categories: OfferCategory[]): Chip[] {
    return categories.map((category) => Chip.of({ id: category.id, label: category.name }));
  }

  onCategoryRemoved(chip: Chip): void {
    const categories = this.selectedCategories$.value.filter((category) => category !== chip.id);
    this.selectedCategories$.next(categories);
  }

  onCategoryAdded(chip: Chip): void {
    const category = this.availableCategories$.value.find((c) => c.id === chip.id);
    if (category) {
      const categories = [...this.selectedCategories$.value, category.id];
      this.selectedCategories$.next(categories);
    }
  }

  editImages(offer: Offer): void {
    const dialog = Dialog.create<EditImagesDialogResult>({
      title: 'Bilder bearbeiten',
      componentType: EditImagesDialog,
      providers: [
        {
          provide: EditImagesDialogData,
          useValue: EditImagesDialogData.of({
            offer: {
              id: offer.id,
              version: offer.version,
            },
            images: offer.images,
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
      dialog
        .getResult()
        .map((result) => offer.updateImages(result.version, result.images))
        .ifSome((updatedOffer) => this.offer$.next(some(updatedOffer)));
    });
  }

  editDescriptionNote(offer: Offer): void {
    this.editNote(offer, NoteType.DESCRIPTION, 'Beschreibung bearbeiten');
  }

  editContainsNote(offer: Offer): void {
    this.editNote(offer, NoteType.CONTAINS, 'Inhaltsangaben bearbeiten');
  }

  editCareNote(offer: Offer): void {
    this.editNote(offer, NoteType.CARE, 'Pflegehinweise bearbeiten');
  }

  editSafetyNote(offer: Offer): void {
    this.editNote(offer, NoteType.SAFETY, 'Sicherheitshinweise bearbeiten');
  }

  editPrice(offer: Offer): void {
    const dialog = Dialog.create<EditPriceDialogResult>({
      title: 'Preis bearbeiten',
      componentType: EditPriceDialog,
      providers: [
        {
          provide: EditPriceDialogData,
          useValue: EditPriceDialogData.of({
            offer: {
              id: offer.id,
              version: offer.version,
            },
            currentPrice: offer.pricing.price,
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
      dialog
        .getResult()
        .map((result) => offer.updatePricing(result.version, offer.pricing.updatePrice(result.version, result.price)))
        .ifSome((updatedOffer) => this.offer$.next(some(updatedOffer)));
    });
  }

  addDiscount(offer: Offer): void {
    const dialog = Dialog.create<AddDiscountDialogResult>({
      title: 'Rabatt hinzufügen',
      componentType: AddDiscountDialog,
      providers: [
        {
          provide: AddDiscountDialogData,
          useValue: AddDiscountDialogData.of({
            offer: {
              id: offer.id,
              version: offer.version,
            },
            currentPrice: offer.pricing.price,
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
      dialog
        .getResult()
        .map((result) =>
          offer.updatePricing(result.version, offer.pricing.addDiscount(result.version, result.discountedPrice)),
        )
        .ifSome((updatedOffer) => this.offer$.next(some(updatedOffer)));
    });
  }

  removeDiscount(offer: Offer): void {
    if (this.actionInProgress$.value) {
      return;
    }
    this.actionInProgress$.next(true);

    this.offersService
      .removeDiscount(offer.id, offer.version)
      .pipe(
        first(),
        finalize(() => this.actionInProgress$.next(false)),
      )
      .subscribe({
        next: (version) => {
          const updatedOffer = offer.updatePricing(version, offer.pricing.removeDiscount(version));
          this.offer$.next(some(updatedOffer));

          this.notificationService.publish({
            message: 'Rabatt wurde entfernt.',
            type: 'success',
          });
        },
        error: (e) => {
          console.error('Failed to remove discount', e);
          this.notificationService.publish({
            message: 'Rabatt konnte nicht entfernt werden. Bitte versuche es erneut.',
            type: 'error',
          });
        },
      });
  }

  publishOffer(offer: Offer): void {
    this.performLifecycleAction(
      offer,
      () => this.offersService.publishOffer(offer.id, offer.version),
      (version) => offer.publish(version),
      'Sofortkauf wurde veröffentlicht.',
      'Sofortkauf konnte nicht veröffentlicht werden.',
    );
  }

  unpublishOffer(offer: Offer): void {
    this.performLifecycleAction(
      offer,
      () => this.offersService.unpublishOffer(offer.id, offer.version),
      (version) => offer.unpublish(version),
      'Veröffentlichung wurde aufgehoben.',
      'Veröffentlichung konnte nicht aufgehoben werden.',
    );
  }

  reserveOffer(offer: Offer): void {
    this.performLifecycleAction(
      offer,
      () => this.offersService.reserveOffer(offer.id, offer.version),
      (version) => offer.reserve(version),
      'Sofortkauf wurde reserviert.',
      'Sofortkauf konnte nicht reserviert werden.',
    );
  }

  unreserveOffer(offer: Offer): void {
    this.performLifecycleAction(
      offer,
      () => this.offersService.unreserveOffer(offer.id, offer.version),
      (version) => offer.unreserve(version),
      'Reservierung wurde aufgehoben.',
      'Reservierung konnte nicht aufgehoben werden.',
    );
  }

  archiveOffer(offer: Offer): void {
    const dialog = Dialog.create<ConfirmArchiveDialogResult>({
      title: 'Sofortkauf archivieren',
      componentType: ConfirmArchiveDialog,
      providers: [],
      environmentInjector: this.environmentInjector,
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog.getResult().ifSome((result) => {
        if (result.confirmed) {
          this.performLifecycleAction(
            offer,
            () => this.offersService.archiveOffer(offer.id, offer.version),
            (version) => offer,
            'Sofortkauf wurde archiviert.',
            'Sofortkauf konnte nicht archiviert werden.',
            true,
          );
        }
      });
    });
  }

  private performLifecycleAction(
    offer: Offer,
    action: () => Observable<number>,
    update: (version: number) => Offer,
    successMessage: string,
    errorMessage: string,
    reload: boolean = false,
  ): void {
    if (this.actionInProgress$.value) {
      return;
    }
    this.actionInProgress$.next(true);

    action()
      .pipe(
        first(),
        finalize(() => this.actionInProgress$.next(false)),
      )
      .subscribe({
        next: (version) => {
          if (reload) {
            this.reloadOffer(offer.id);
          } else {
            this.offer$.next(some(update(version)));
          }

          this.notificationService.publish({
            message: successMessage,
            type: 'success',
          });
        },
        error: (e) => {
          console.error('Lifecycle action failed', e);
          this.notificationService.publish({
            message: `${errorMessage} Bitte versuche es erneut.`,
            type: 'error',
          });
        },
      });
  }

  private editNote(offer: Offer, noteType: NoteType, title: string): void {
    const dialog = Dialog.create<EditNoteDialogResult>({
      title,
      componentType: EditNoteDialog,
      providers: [
        {
          provide: EditNoteDialogData,
          useValue: EditNoteDialogData.of({
            offer: {
              id: offer.id,
              version: offer.version,
            },
            notes: offer.notes,
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
      dialog
        .getResult()
        .map((result) => offer.updateNotes(result.version, result.notes))
        .ifSome((updatedOffer) => this.offer$.next(some(updatedOffer)));
    });
  }

  private toImageSliderImages(images: string[]): ImageSliderImage[] {
    return images.map((image) => ImageSliderImage.of({ url: this.getImageUrl(image) }));
  }

  private getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  private saveUpdatedCategories(offer: Offer, categories: OfferCategoryId[]): void {
    this.offersService
      .updateCategories({ id: offer.id, version: offer.version, categoryIds: categories })
      .pipe(first())
      .subscribe({
        next: (version) => {
          const updatedOffer = offer.updateCategories(version, new Set(categories));
          this.offer$.next(some(updatedOffer));

          this.notificationService.publish({
            message: 'Kategorien wurden aktualisiert.',
            type: 'success',
          });
        },
        error: (e) => {
          console.error('Failed to update categories', e);
          this.notificationService.publish({
            message: 'Kategorien konnten nicht aktualisiert werden. Bitte versuche es erneut.',
            type: 'error',
          });
        },
      });
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

  private areSetsEqual<T>(a: Set<T>, b: Set<T>): boolean {
    if (a.size !== b.size) {
      return false;
    }

    for (const item of a) {
      if (!b.has(item)) {
        return false;
      }
    }

    return true;
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
        map((offer) => some(offer)),
        catchError((e) => {
          console.error('Failed to load offer', e);
          this.notificationService.publish({
            message: 'Sofortkauf konnte nicht geladen werden. Bitte versuche die Seite neu zu laden.',
            type: 'error',
          });

          return of(none<Offer>());
        }),
        takeUntil(this.destroy$),
        finalize(() => {
          this.loadingOffer$.next(false);
          this.offerLoaded$.next(true);
        }),
      )
      .subscribe((offer) => {
        this.offer$.next(offer);
        offer.ifSome((o) => {
          this.title$.next(o.title);
          this.size$.next(o.size);
          this.selectedCategories$.next(Array.from(o.categories));
        });
      });
  }
}
