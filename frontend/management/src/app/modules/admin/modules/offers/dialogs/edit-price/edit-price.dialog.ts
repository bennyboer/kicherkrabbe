import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { BehaviorSubject, finalize, first, Subject } from 'rxjs';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { NotificationService } from '../../../../../shared';
import { OffersService } from '../../services';
import { Money } from '../../model';
import { Option, someOrNone, validateProps } from '@kicherkrabbe/shared';

export class EditPriceDialogData {
  readonly offer: Option<{ id: string; version: number }>;
  readonly currentPrice: Money;

  private constructor(props: { offer: Option<{ id: string; version: number }>; currentPrice: Money }) {
    validateProps(props);

    this.offer = props.offer;
    this.currentPrice = props.currentPrice;
  }

  static of(props: {
    offer?: { id: string; version: number };
    currentPrice?: Money;
  }): EditPriceDialogData {
    return new EditPriceDialogData({
      offer: someOrNone(props.offer),
      currentPrice: someOrNone(props.currentPrice).orElse(Money.euro(0)),
    });
  }
}

export interface EditPriceDialogResult {
  version: number;
  price: Money;
}

@Component({
  selector: 'app-edit-price-dialog',
  templateUrl: './edit-price.dialog.html',
  styleUrls: ['./edit-price.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class EditPriceDialog implements OnDestroy {
  protected readonly saving$ = new BehaviorSubject<boolean>(false);
  protected readonly priceInEuros$ = new BehaviorSubject<string>('');

  protected readonly cannotSave$ = this.saving$.asObservable();
  protected readonly loading$ = this.saving$.asObservable();

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly data: EditPriceDialogData,
    private readonly dialog: Dialog<EditPriceDialogResult>,
    private readonly dialogService: DialogService,
    private readonly offersService: OffersService,
    private readonly notificationService: NotificationService,
  ) {
    const euros = (data.currentPrice.amount / 100).toFixed(2).replace('.', ',');
    this.priceInEuros$.next(euros);
  }

  ngOnDestroy(): void {
    this.saving$.complete();
    this.priceInEuros$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updatePrice(value: string): void {
    this.priceInEuros$.next(value);
  }

  cancel(): void {
    this.dialogService.close(this.dialog.id);
  }

  save(): void {
    if (this.saving$.value) {
      return;
    }

    const cents = this.parseCents(this.priceInEuros$.value);
    if (cents === null || cents <= 0) {
      this.notificationService.publish({
        message: 'Bitte geben Sie einen gültigen Preis ein.',
        type: 'error',
      });
      return;
    }

    this.saving$.next(true);
    const price = Money.euro(cents);

    this.data.offer.ifSomeOrElse(
      (offer) =>
        this.offersService
          .updatePrice({
            id: offer.id,
            version: offer.version,
            price,
          })
          .pipe(
            first(),
            finalize(() => this.saving$.next(false)),
          )
          .subscribe({
            next: (version) => {
              this.notificationService.publish({
                message: 'Preis wurde aktualisiert.',
                type: 'success',
              });

              this.dialog.attachResult({ version, price });
              this.dialogService.close(this.dialog.id);
            },
            error: (e) => {
              console.error('Failed to update price', e);
              this.notificationService.publish({
                message: 'Preis konnte nicht aktualisiert werden. Bitte versuchen Sie es erneut.',
                type: 'error',
              });
            },
          }),
      () => {
        this.dialog.attachResult({ version: 0, price });
        this.dialogService.close(this.dialog.id);
      },
    );
  }

  private parseCents(value: string): number | null {
    const normalized = value.replace(',', '.');
    const parsed = parseFloat(normalized);
    if (isNaN(parsed)) {
      return null;
    }
    return Math.round(parsed * 100);
  }
}
