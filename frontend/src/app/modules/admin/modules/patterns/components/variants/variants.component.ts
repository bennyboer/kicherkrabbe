import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { PatternVariant } from '../../model';
import { someOrNone, validateProps } from '../../../../../../util';
import { ButtonSize } from '../../../../../shared';

class EditablePatternVariant {
  readonly id: string;
  readonly variant: PatternVariant;
  readonly editing: boolean;
  readonly expanded: boolean;

  private constructor(props: {
    variant: PatternVariant;
    editing: boolean;
    expanded: boolean;
  }) {
    validateProps(props);

    this.id = crypto.randomUUID();
    this.variant = props.variant;
    this.editing = props.editing;
    this.expanded = props.expanded;
  }

  static of(props: {
    variant: PatternVariant;
    editing?: boolean;
    expanded?: boolean;
  }): EditablePatternVariant {
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
      variant: PatternVariant.of({ name, sizes: this.variant.sizes }),
    });
  }
}

@Component({
  selector: 'app-variants',
  templateUrl: './variants.component.html',
  styleUrls: ['./variants.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VariantsComponent implements OnDestroy {
  protected readonly variants$: BehaviorSubject<EditablePatternVariant[]> =
    new BehaviorSubject<EditablePatternVariant[]>([]);

  protected readonly sortableConfig: any = {
    onUpdate: () => this.variants$.next(this.variants$.value),
    draggable: '.item',
  };

  protected readonly ButtonSize = ButtonSize;

  ngOnDestroy(): void {
    this.variants$.complete();
  }

  add(): void {
    const newVariant = EditablePatternVariant.of({
      variant: PatternVariant.of({ name: '', sizes: [] }),
    }).startEditing();

    this.variants$.next([...this.variants$.value, newVariant]);
  }

  save(variant: EditablePatternVariant, name: string): void {
    const updatedVariants = this.variants$.value.map((v) =>
      v.id === variant.id ? variant.stopEditing().withName(name) : v,
    );

    this.variants$.next(updatedVariants);
  }

  cancel(variant: EditablePatternVariant): void {
    const updatedVariants = this.variants$.value.map((v) =>
      v.id === variant.id ? variant.stopEditing() : v,
    );

    this.variants$.next(updatedVariants);
  }

  edit(variant: EditablePatternVariant): void {
    const updatedVariants = this.variants$.value.map((v) =>
      v.id === variant.id ? variant.startEditing() : v,
    );

    this.variants$.next(updatedVariants);
  }

  delete(variant: EditablePatternVariant): void {
    const updatedVariants = this.variants$.value.filter(
      (v) => v.id !== variant.id,
    );

    this.variants$.next(updatedVariants);
  }

  toggle(variant: EditablePatternVariant): void {
    const updatedVariants = this.variants$.value.map((v) =>
      v.id === variant.id ? variant.toggle() : v,
    );

    this.variants$.next(updatedVariants);
  }
}
