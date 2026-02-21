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
import { Offer, OfferStatus } from '../../model';
import { OffersService } from '../../services';
import { environment } from '../../../../../../../environments';
import { ImageSliderImage } from '../../../../../shared/modules/image-slider';
import { Theme, ThemeService } from '../../../../../../services';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import {
  AddDiscountDialog,
  AddDiscountDialogData,
  AddDiscountDialogResult,
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

  protected readonly loading$ = this.loadingOffer$.asObservable();
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
    private readonly assetsService: AssetsService,
    private readonly notificationService: NotificationService,
    private readonly themeService: ThemeService,
    private readonly dialogService: DialogService,
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
    this.offerLoaded$.complete();
    this.loadingOffer$.complete();
    this.actionInProgress$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  editImages(offer: Offer): void {
    const dialog = Dialog.create<EditImagesDialogResult>({
      title: 'Bilder bearbeiten',
      componentType: EditImagesDialog,
      injector: Injector.create({
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
      }),
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
      injector: Injector.create({
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
      }),
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
      injector: Injector.create({
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
      }),
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
    this.performLifecycleAction(
      offer,
      () => this.offersService.archiveOffer(offer.id, offer.version),
      (version) => offer,
      'Sofortkauf wurde archiviert.',
      'Sofortkauf konnte nicht archiviert werden.',
      true,
    );
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
      injector: Injector.create({
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
      }),
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
      .subscribe((offer) => this.offer$.next(offer));
  }
}
