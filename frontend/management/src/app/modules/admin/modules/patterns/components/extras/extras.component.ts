import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core';
import { ButtonSize } from '../../../../../shared';
import { BehaviorSubject, Subject } from 'rxjs';
import { PatternExtra } from '../../model';
import { Money } from '../../../../../../util';
import currency from 'currency.js';
import { someOrNone, validateProps } from '@kicherkrabbe/shared';

class EditablePatternExtra {
  readonly extra: PatternExtra;
  readonly editing: boolean;

  private constructor(props: { extra: PatternExtra; editing: boolean }) {
    validateProps(props);

    this.extra = props.extra;
    this.editing = props.editing;
  }

  static of(props: { extra: PatternExtra; editing?: boolean }): EditablePatternExtra {
    return new EditablePatternExtra({
      extra: props.extra,
      editing: someOrNone(props.editing).orElse(false),
    });
  }

  startEditing(): EditablePatternExtra {
    return EditablePatternExtra.of({
      ...this,
      editing: true,
    });
  }

  stopEditing(): EditablePatternExtra {
    return EditablePatternExtra.of({
      ...this,
      editing: false,
    });
  }

  withName(name: string): EditablePatternExtra {
    return EditablePatternExtra.of({
      ...this,
      extra: PatternExtra.of({ name, price: this.extra.price }),
    });
  }

  withPrice(price: Money): EditablePatternExtra {
    return EditablePatternExtra.of({
      ...this,
      extra: PatternExtra.of({ name: this.extra.name, price: price }),
    });
  }
}

@Component({
  selector: 'app-extras',
  templateUrl: './extras.component.html',
  styleUrls: ['./extras.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class ExtrasComponent implements OnDestroy {
  @Input()
  set extras(extras: PatternExtra[] | null) {
    someOrNone(extras).ifSome((extras) => {
      this.extras$.next(extras.map((extra) => EditablePatternExtra.of({ extra })));
    });
  }

  @Output()
  changed: EventEmitter<PatternExtra[]> = new EventEmitter<PatternExtra[]>();

  protected readonly extras$: BehaviorSubject<EditablePatternExtra[]> = new BehaviorSubject<EditablePatternExtra[]>([]);
  private readonly destroy$: Subject<void> = new Subject<void>();

  protected readonly ButtonSize = ButtonSize;

  protected readonly sortableConfig: any = {
    onUpdate: () => {
      this.extras$.next(this.extras$.value);
      this.emitChange();
    },
  };

  ngOnDestroy(): void {
    this.extras$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  add(): void {
    const newExtra = EditablePatternExtra.of({
      extra: PatternExtra.of({ name: '' }),
    }).startEditing();

    this.extras$.next([...this.extras$.value, newExtra]);
  }

  edit(extra: EditablePatternExtra): void {
    const updatedExtras = this.extras$.value.map((e) => (e === extra ? extra.startEditing() : e));
    this.extras$.next(updatedExtras);
  }

  save(extra: EditablePatternExtra, name: string, price: string): void {
    const nameTrimmed = name.trim();

    const parsedPrice = currency(price, {
      symbol: '€',
      separator: '.',
      decimal: ',',
    });
    const priceAsMoney = Money.euro(parsedPrice.intValue);

    const updatedExtras = this.extras$.value.map((e) => {
      if (e === extra) {
        return e.stopEditing().withName(nameTrimmed).withPrice(priceAsMoney);
      }

      return e;
    });

    this.extras$.next(updatedExtras);
    this.emitChange();
  }

  delete(extra: EditablePatternExtra): void {
    const updatedExtras = this.extras$.value.filter((e) => e !== extra);
    this.extras$.next(updatedExtras);
    this.emitChange();
  }

  cancel(extra: EditablePatternExtra): void {
    const updatedExtras = this.extras$.value.map((e) => (e === extra ? e.stopEditing() : e));
    this.extras$.next(updatedExtras);
  }

  private emitChange(): void {
    this.changed.emit(this.extras$.value.map((e) => e.extra));
  }
}
