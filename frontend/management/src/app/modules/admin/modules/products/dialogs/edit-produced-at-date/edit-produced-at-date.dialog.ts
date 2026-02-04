import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { ProductsService } from '../../services';
import { NotificationService } from '../../../../../shared';
import { BehaviorSubject, combineLatest, finalize, first, map } from 'rxjs';
import { none, Option, someOrNone, validateProps } from '@kicherkrabbe/shared';

export class EditProducedAtDateDialogData {
  readonly product: Option<{ id: string; version: number }>;

  private constructor(props: { product: Option<{ id: string; version: number }> }) {
    validateProps(props);

    this.product = props.product;
  }

  static of(props: { product?: { id: string; version: number } }): EditProducedAtDateDialogData {
    return new EditProducedAtDateDialogData({
      product: someOrNone(props.product),
    });
  }
}

export interface EditProducedAtDateDialogResult {
  version: number;
  date: Date;
}

@Component({
  selector: 'app-edit-date-dialog',
  templateUrl: './edit-produced-at-date.dialog.html',
  styleUrls: ['./edit-produced-at-date.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class EditProducedAtDateDialog implements OnDestroy {
  protected readonly date$ = new BehaviorSubject<Option<Date>>(none());
  protected readonly time$ = new BehaviorSubject<Option<Date>>(none());
  protected readonly saving$ = new BehaviorSubject<boolean>(false);

  protected readonly cannotSave$ = combineLatest([this.date$, this.time$, this.saving$]).pipe(
    map(([date, time, saving]) => date.isNone() || time.isNone() || saving),
  );
  protected readonly loading$ = this.saving$.asObservable();

  constructor(
    private readonly data: EditProducedAtDateDialogData,
    private readonly dialog: Dialog<EditProducedAtDateDialogResult>,
    private readonly dialogService: DialogService,
    private readonly productsService: ProductsService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnDestroy(): void {
    this.date$.complete();
    this.saving$.complete();
  }

  updateDate(date: Date | null): void {
    this.date$.next(someOrNone(date));
  }

  updateTime(time: Date | null): void {
    this.time$.next(someOrNone(time));
  }

  cancel(): void {
    this.dialogService.close(this.dialog.id);
  }

  save(): void {
    if (this.saving$.value) {
      return;
    }
    this.saving$.next(true);

    if (this.date$.value.isNone() || this.time$.value.isNone()) {
      return;
    }

    const date = this.date$.value.orElseThrow();
    const time = this.time$.value.orElseThrow();
    const timezoneAdjustedTime = new Date(time.getTime() + time.getTimezoneOffset() * 60000);

    const totalDate = new Date(date.getTime() + timezoneAdjustedTime.getTime());

    this.data.product.ifSomeOrElse(
      (product) =>
        this.productsService
          .updateProducedAtDate({
            id: product.id,
            version: product.version,
            date: totalDate,
          })
          .pipe(
            first(),
            finalize(() => this.saving$.next(false)),
          )
          .subscribe({
            next: (version) => {
              this.notificationService.publish({
                message: 'Produktionsdatum wurde erfolgreich aktualisiert',
                type: 'success',
              });

              this.dialog.attachResult({
                version,
                date: totalDate,
              });
              this.dialogService.close(this.dialog.id);
            },
            error: (e) => {
              console.error('Failed to update produced at date', e);
              this.notificationService.publish({
                message: 'Fehler beim Aktualisieren des Produktionsdatums. Bitte versuche es erneut.',
                type: 'error',
              });
            },
          }),
      () => {
        this.dialog.attachResult({
          version: 0,
          date: totalDate,
        });
        this.dialogService.close(this.dialog.id);
      },
    );
  }
}
