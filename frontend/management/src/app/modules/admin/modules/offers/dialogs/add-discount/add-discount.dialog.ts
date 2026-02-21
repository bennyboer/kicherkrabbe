import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { BehaviorSubject, finalize, first, Subject } from 'rxjs';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { NotificationService } from '../../../../../shared';
import { OffersService } from '../../services';
import { Money } from '../../model';
import { validateProps } from '@kicherkrabbe/shared';

export class AddDiscountDialogData {
  readonly offer: { id: string; version: number };
  readonly currentPrice: Money;

  private constructor(props: { offer: { id: string; version: number }; currentPrice: Money }) {
    validateProps(props);

    this.offer = props.offer;
    this.currentPrice = props.currentPrice;
  }

  static of(props: { offer: { id: string; version: number }; currentPrice: Money }): AddDiscountDialogData {
    return new AddDiscountDialogData({
      offer: props.offer,
      currentPrice: props.currentPrice,
    });
  }
}

export interface AddDiscountDialogResult {
  version: number;
  discountedPrice: Money;
}

@Component({
  selector: 'app-add-discount-dialog',
  templateUrl: './add-discount.dialog.html',
  styleUrls: ['./add-discount.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class AddDiscountDialog implements OnDestroy {
  protected readonly saving$ = new BehaviorSubject<boolean>(false);
  protected readonly discountedPriceInEuros$ = new BehaviorSubject<string>('');

  protected readonly cannotSave$ = this.saving$.asObservable();
  protected readonly loading$ = this.saving$.asObservable();

  protected readonly currentPriceDisplay: string;

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly data: AddDiscountDialogData,
    private readonly dialog: Dialog<AddDiscountDialogResult>,
    private readonly dialogService: DialogService,
    private readonly offersService: OffersService,
    private readonly notificationService: NotificationService,
  ) {
    this.currentPriceDisplay = data.currentPrice.toDisplayString();
  }

  ngOnDestroy(): void {
    this.saving$.complete();
    this.discountedPriceInEuros$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateDiscountedPrice(value: string): void {
    this.discountedPriceInEuros$.next(value);
  }

  cancel(): void {
    this.dialogService.close(this.dialog.id);
  }

  save(): void {
    if (this.saving$.value) {
      return;
    }

    const cents = this.parseCents(this.discountedPriceInEuros$.value);
    if (cents === null || cents <= 0) {
      this.notificationService.publish({
        message: 'Bitte geben Sie einen gültigen reduzierten Preis ein.',
        type: 'error',
      });
      return;
    }

    if (cents >= this.data.currentPrice.amount) {
      this.notificationService.publish({
        message: 'Der reduzierte Preis muss kleiner als der aktuelle Preis sein.',
        type: 'error',
      });
      return;
    }

    this.saving$.next(true);
    const discountedPrice = Money.euro(cents);

    this.offersService
      .addDiscount({
        id: this.data.offer.id,
        version: this.data.offer.version,
        discountedPrice,
      })
      .pipe(
        first(),
        finalize(() => this.saving$.next(false)),
      )
      .subscribe({
        next: (version) => {
          this.notificationService.publish({
            message: 'Rabatt wurde hinzugefügt.',
            type: 'success',
          });

          this.dialog.attachResult({ version, discountedPrice });
          this.dialogService.close(this.dialog.id);
        },
        error: (e) => {
          console.error('Failed to add discount', e);
          this.notificationService.publish({
            message: 'Rabatt konnte nicht hinzugefügt werden. Bitte versuchen Sie es erneut.',
            type: 'error',
          });
        },
      });
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
