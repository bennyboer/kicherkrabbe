import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnDestroy,
} from '@angular/core';
import { DropdownItem } from '../dropdown/dropdown.component';
import { BehaviorSubject, map, Observable, ReplaySubject, Subject } from 'rxjs';
import { Option } from '../../../../util';

export type FilterItemId = string;

export interface FilterItem {
  id: FilterItemId;
  label: string;
}

export enum FilterSelectionMode {
  SINGLE = 'SINGLE',
  MULTIPLE = 'MULTIPLE',
}

class FilterDropdownItem implements DropdownItem {
  readonly id: FilterItemId;
  readonly label: string;

  private constructor(props: { id: FilterItemId; label: string }) {
    this.id = props.id;
    this.label = props.label;
  }

  static fromFilterItem(item: FilterItem): FilterDropdownItem {
    return new FilterDropdownItem({
      id: item.id,
      label: item.label,
    });
  }
}

export class Filter {
  readonly id: string;
  readonly placeholder: string;
  readonly items: FilterItem[];
  readonly selectionMode: FilterSelectionMode = FilterSelectionMode.SINGLE;

  private readonly selected$: BehaviorSubject<Set<FilterItemId>> =
    new BehaviorSubject<Set<FilterItemId>>(new Set<FilterItemId>());

  private constructor(props: {
    id: string;
    placeholder: string;
    items: FilterItem[];
    selectionMode: Option<FilterSelectionMode>;
  }) {
    this.id = props.id;
    this.placeholder = props.placeholder;
    this.items = props.items;
    this.selectionMode = props.selectionMode.orElse(FilterSelectionMode.SINGLE);

    if (this.selectionMode === FilterSelectionMode.MULTIPLE) {
      throw new Error('Multiple selection mode is not supported yet'); // TODO Implement multiple selection mode
    }
  }

  static of(props: {
    id: string;
    placeholder: string;
    items: FilterItem[];
    selectionMode?: FilterSelectionMode;
  }): Filter {
    if (!props.placeholder || props.placeholder.trim().length === 0) {
      throw new Error('Placeholder is required');
    }

    if (!props.items || props.items.length === 0) {
      throw new Error('Items are required');
    }

    return new Filter({
      id: props.id,
      placeholder: props.placeholder,
      items: props.items,
      selectionMode: Option.someOrNone(props.selectionMode),
    });
  }

  getSelected(): Observable<FilterItemId[]> {
    return this.selected$.asObservable().pipe(map((set) => Array.from(set)));
  }

  isSelected(id: FilterItemId): boolean {
    return this.selected$.value.has(id);
  }

  toggle(id: FilterItemId): void {
    const isSingleSelectionMode =
      this.selectionMode === FilterSelectionMode.SINGLE;

    const updatedSet = new Set([...this.selected$.value]);

    if (this.isSelected(id)) {
      const updated = updatedSet.delete(id);
      if (updated) {
        this.selected$.next(updatedSet);
      }
    } else {
      if (isSingleSelectionMode) {
        updatedSet.clear();
      }

      updatedSet.add(id);
      this.selected$.next(updatedSet);
    }
  }
}

@Component({
  selector: 'app-filter-sort-bar',
  templateUrl: './filter-sort-bar.component.html',
  styleUrls: ['./filter-sort-bar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FilterSortBarComponent implements OnDestroy {
  @Input('filters')
  set setFilters(filters: Filter[]) {
    Option.someOrNone(filters).ifSome((filters) => this.filters$.next(filters));
  }

  // TODO Set sorting options

  // TODO Remove when having sorting options
  testItems: DropdownItem[] = [
    { id: 'Item 1' },
    {
      id: 'Long item tralalala fjiwej  fwefjwef wefjjewfijwe fwje fiojwe fjweiojf ewifj we2',
    },
    { id: 'Item 3' },
  ];

  protected filters$: Subject<Filter[]> = new ReplaySubject<Filter[]>(1);

  ngOnDestroy(): void {
    this.filters$.complete();
  }

  protected filterToDropdownItems(items: FilterItem[]): DropdownItem[] {
    return items.map((item) => this.filterToDropdownItem(item));
  }

  private filterToDropdownItem(item: FilterItem): DropdownItem {
    return FilterDropdownItem.fromFilterItem(item);
  }
}
