import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  Output,
} from '@angular/core';
import { PricedSizeRange } from '../../model';
import { BehaviorSubject } from 'rxjs';
import { Money, validateProps } from '../../../../../../util';
import { ButtonSize } from '../../../../../shared';
import currency from 'currency.js';
import { none, Option, someOrNone } from '../../../../../shared/modules/option';

class EditablePricedSizeRange {
  readonly size: PricedSizeRange;
  readonly editing: boolean;

  private constructor(props: { size: PricedSizeRange; editing: boolean }) {
    validateProps(props);

    this.size = props.size;
    this.editing = props.editing;
  }

  static of(props: {
    size: PricedSizeRange;
    editing?: boolean;
  }): EditablePricedSizeRange {
    return new EditablePricedSizeRange({
      size: props.size,
      editing: someOrNone(props.editing).orElse(false),
    });
  }

  startEditing(): EditablePricedSizeRange {
    return EditablePricedSizeRange.of({
      ...this,
      editing: true,
    });
  }

  stopEditing(): EditablePricedSizeRange {
    return EditablePricedSizeRange.of({
      ...this,
      editing: false,
    });
  }

  withSize(size: PricedSizeRange): EditablePricedSizeRange {
    return EditablePricedSizeRange.of({
      ...this,
      size,
    });
  }
}

@Component({
  selector: 'app-sizes',
  templateUrl: './sizes.component.html',
  styleUrls: ['./sizes.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SizesComponent implements OnDestroy {
  @Input()
  set sizes(sizes: PricedSizeRange[] | null) {
    someOrNone(sizes)
      .map((s) =>
        s.map((size) => {
          const id = size.id;
          const old = someOrNone(
            this.sizes$.value.find((s) => s.size.id === id),
          );

          return old
            .map((o) =>
              EditablePricedSizeRange.of({ size, editing: o.editing }),
            )
            .orElseGet(() => EditablePricedSizeRange.of({ size }));
        }),
      )
      .ifSome((sizes) => this.sizes$.next(sizes));
  }

  @Output()
  changed: EventEmitter<PricedSizeRange[]> = new EventEmitter<
    PricedSizeRange[]
  >();

  protected sizes$: BehaviorSubject<EditablePricedSizeRange[]> =
    new BehaviorSubject<EditablePricedSizeRange[]>([]);

  protected readonly ButtonSize = ButtonSize;

  ngOnDestroy(): void {
    this.sizes$.complete();
  }

  add(): void {
    this.sizes$.next([
      ...this.sizes$.value,
      EditablePricedSizeRange.of({
        size: PricedSizeRange.of({ from: 0, price: Money.zero() }),
      }).startEditing(),
    ]);
  }

  edit(size: EditablePricedSizeRange): void {
    const updatedSizes = this.sizes$.value.map((s) =>
      s.size.id === size.size.id ? s.startEditing() : s,
    );
    this.sizes$.next(updatedSizes);
  }

  delete(size: EditablePricedSizeRange): void {
    const updatedSizes = this.sizes$.value.filter(
      (s) => s.size.id !== size.size.id,
    );
    this.sizes$.next(updatedSizes);
    this.emitChange();
  }

  stringifyOptionalNumber(num: Option<number>, fallback: string): string {
    return num.map((n) => n.toString()).orElse(fallback);
  }

  cancel(size: EditablePricedSizeRange): void {
    const updatedSizes = this.sizes$.value.map((s) =>
      s.size.id === size.size.id ? s.stopEditing() : s,
    );
    this.sizes$.next(updatedSizes);
  }

  save(
    size: EditablePricedSizeRange,
    from: string,
    to: string,
    unit: string,
    price: string,
  ): void {
    let fromSize = 0;
    if (from) {
      fromSize = parseInt(from, 10);
    }

    let toSize: Option<number> = none();
    if (to) {
      toSize = someOrNone(parseInt(to, 10));
    }

    let updatedUnit: Option<string> = none();
    if (unit) {
      updatedUnit = someOrNone(unit);
    }

    const parsedPrice = currency(price, {
      symbol: '€',
      separator: '.',
      decimal: ',',
    });
    const priceAsMoney = Money.euro(parsedPrice.intValue);

    const updatedSize = size.size
      .withFrom(fromSize)
      .withTo(toSize.orElseNull())
      .withUnit(updatedUnit.orElseNull())
      .withPrice(priceAsMoney);

    const updatedSizes = this.sizes$.value.map((s) => {
      if (s.size.id === size.size.id) {
        return s.stopEditing().withSize(updatedSize);
      }

      return s;
    });
    this.sizes$.next(updatedSizes);
    this.emitChange();
  }

  private emitChange(): void {
    this.changed.emit(this.sizes$.value.map((size) => size.size));
  }
}
