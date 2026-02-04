import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { BehaviorSubject, fromEvent, map, startWith, Subject, takeUntil } from 'rxjs';
import { PatternVariant, PricedSizeRange } from '../../model';
import { ButtonSize } from '../../../../../shared';
import { someOrNone, validateProps } from '@kicherkrabbe/shared';

class EditablePatternVariant {
  readonly variant: PatternVariant;
  readonly editing: boolean;
  readonly expanded: boolean;

  private constructor(props: { variant: PatternVariant; editing: boolean; expanded: boolean }) {
    validateProps(props);

    this.variant = props.variant;
    this.editing = props.editing;
    this.expanded = props.expanded;
  }

  static of(props: { variant: PatternVariant; editing?: boolean; expanded?: boolean }): EditablePatternVariant {
    return new EditablePatternVariant({
      variant: props.variant,
      editing: someOrNone(props.editing).orElse(false),
      expanded: someOrNone(props.expanded).orElse(false),
    });
  }

  startEditing(): EditablePatternVariant {
    return EditablePatternVariant.of({
      ...this,
      editing: true,
    });
  }

  stopEditing(): EditablePatternVariant {
    return EditablePatternVariant.of({
      ...this,
      editing: false,
    });
  }

  toggle(): EditablePatternVariant {
    return EditablePatternVariant.of({
      ...this,
      expanded: !this.expanded,
    });
  }

  withName(name: string): EditablePatternVariant {
    return EditablePatternVariant.of({
      ...this,
      variant: this.variant.withName(name),
    });
  }

  withSizes(sizes: PricedSizeRange[]): EditablePatternVariant {
    return EditablePatternVariant.of({
      ...this,
      variant: this.variant.withSizes(sizes),
    });
  }
}

@Component({
  selector: 'app-variants',
  templateUrl: './variants.component.html',
  styleUrls: ['./variants.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class VariantsComponent implements OnInit, OnDestroy {
  @Input()
  set variants(extras: PatternVariant[] | null) {
    someOrNone(extras).ifSome((extras) => {
      this.variants$.next(
        extras.map((variant) => {
          const id = variant.id;
          const old = someOrNone(this.variants$.value.find((v) => v.variant.id === id));

          return old
            .map((o) =>
              EditablePatternVariant.of({
                variant,
                editing: o.editing,
                expanded: o.expanded,
              }),
            )
            .orElseGet(() => EditablePatternVariant.of({ variant }));
        }),
      );
    });
  }

  @Output()
  changed: EventEmitter<PatternVariant[]> = new EventEmitter<PatternVariant[]>();

  protected readonly variants$: BehaviorSubject<EditablePatternVariant[]> = new BehaviorSubject<
    EditablePatternVariant[]
  >([]);

  protected readonly sortableConfig: any = {
    onUpdate: () => {
      this.variants$.next(this.variants$.value);
      this.emitChange();
    },
    draggable: '.item',
  };

  protected readonly ButtonSize = ButtonSize;

  protected isMobile: boolean = false;

  private readonly destroy$: Subject<void> = new Subject<void>();

  ngOnInit(): void {
    const mediaQueryList = window.matchMedia('(max-width: 1000px)');

    fromEvent(mediaQueryList, 'change')
      .pipe(
        map(() => mediaQueryList.matches),
        startWith(mediaQueryList.matches),
        takeUntil(this.destroy$),
      )
      .subscribe((isMobile) => (this.isMobile = isMobile));
  }

  ngOnDestroy(): void {
    this.variants$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  add(): void {
    const newVariant = EditablePatternVariant.of({
      variant: PatternVariant.of({ name: '' }),
    }).startEditing();

    this.variants$.next([...this.variants$.value, newVariant]);
  }

  save(variant: EditablePatternVariant, name: string): void {
    const updatedVariants = this.variants$.value.map((v) =>
      v.variant.id === variant.variant.id ? variant.stopEditing().withName(name) : v,
    );

    this.variants$.next(updatedVariants);
    this.emitChange();
  }

  cancel(variant: EditablePatternVariant): void {
    const updatedVariants = this.variants$.value.map((v) =>
      v.variant.id === variant.variant.id ? variant.stopEditing() : v,
    );

    this.variants$.next(updatedVariants);
  }

  edit(variant: EditablePatternVariant): void {
    const updatedVariants = this.variants$.value.map((v) =>
      v.variant.id === variant.variant.id ? variant.startEditing() : v,
    );

    this.variants$.next(updatedVariants);
  }

  delete(variant: EditablePatternVariant): void {
    const updatedVariants = this.variants$.value.filter((v) => v.variant.id !== variant.variant.id);

    this.variants$.next(updatedVariants);
    this.emitChange();
  }

  toggle(variant: EditablePatternVariant): void {
    const updatedVariants = this.variants$.value.map((v) =>
      v.variant.id === variant.variant.id ? variant.toggle() : v,
    );

    this.variants$.next(updatedVariants);
  }

  onVariantSizesChanged(variant: EditablePatternVariant, sizes: PricedSizeRange[]): void {
    const updatedVariants = this.variants$.value.map((v) =>
      v.variant.id === variant.variant.id ? v.withSizes(sizes) : v,
    );

    this.variants$.next(updatedVariants);
    this.emitChange();
  }

  getVisibleColumnCount(): number {
    return this.isMobile ? 4 : 6;
  }

  private emitChange(): void {
    this.changed.emit(this.variants$.value.map((v) => v.variant));
  }
}
