import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  TemplateRef,
} from '@angular/core';
import { DropdownItem, DropdownItemId } from '../dropdown/dropdown.component';
import {
  BehaviorSubject,
  combineLatest,
  distinctUntilChanged,
  filter,
  map,
  Observable,
  ReplaySubject,
  Subject,
  take,
  takeUntil,
} from 'rxjs';
import { Eq } from '../../../../util';
import { Option, someOrNone } from '@kicherkrabbe/shared';

export type FilterId = string;
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
  readonly content: any;

  private constructor(props: { id: FilterItemId; label: string; content: any }) {
    this.id = props.id;
    this.label = props.label;
    this.content = props.content;
  }

  static fromFilterItem(item: FilterItem): FilterDropdownItem {
    return new FilterDropdownItem({
      id: item.id,
      label: item.label,
      content: item,
    });
  }
}

export class Filter implements Eq<Filter> {
  readonly id: FilterId;
  readonly label: string;
  readonly items: FilterItem[];
  readonly selectionMode: FilterSelectionMode = FilterSelectionMode.SINGLE;
  readonly itemTemplateRef: Option<TemplateRef<any>>;

  private readonly selected$: BehaviorSubject<Set<FilterItemId>>;

  private constructor(props: {
    id: FilterId;
    label: string;
    items: FilterItem[];
    selectionMode: Option<FilterSelectionMode>;
    itemTemplateRef: Option<TemplateRef<any>>;
    selected: Set<FilterItemId>;
  }) {
    this.id = props.id;
    this.label = props.label;
    this.items = props.items;
    this.selectionMode = props.selectionMode.orElse(FilterSelectionMode.SINGLE);
    this.itemTemplateRef = props.itemTemplateRef;
    this.selected$ = new BehaviorSubject<Set<FilterItemId>>(props.selected);
  }

  static of(props: {
    id: FilterId;
    label: string;
    items: FilterItem[];
    selectionMode?: FilterSelectionMode;
    itemTemplateRef?: TemplateRef<any>;
  }): Filter {
    if (!props.label || props.label.trim().length === 0) {
      throw new Error('Label is required');
    }

    if (!props.items || props.items.length === 0) {
      throw new Error('Items are required');
    }

    return new Filter({
      id: props.id,
      label: props.label,
      items: props.items,
      selectionMode: someOrNone(props.selectionMode),
      itemTemplateRef: someOrNone(props.itemTemplateRef),
      selected: new Set<FilterItemId>(),
    });
  }

  canSelectMultiple(): boolean {
    return this.selectionMode === FilterSelectionMode.MULTIPLE;
  }

  getSelected(): FilterItemId[] {
    return Array.from(this.selected$.value);
  }

  setSelected(ids: FilterItemId[]): void {
    const updatedSet = new Set(ids);
    this.selected$.next(updatedSet);
  }

  isSelected(id: FilterItemId): boolean {
    return this.selected$.value.has(id);
  }

  isActive(): boolean {
    return this.selected$.value.size > 0;
  }

  equals(other: Filter): boolean {
    if (this.id !== other.id) {
      return false;
    }

    if (this.items.length !== other.items.length) {
      return false;
    }

    if (this.selected$.value.size !== other.selected$.value.size) {
      return false;
    }

    const itemsEqual = this.items.every((item, index) => {
      const otherItem = other.items[index];
      return item.id === otherItem.id;
    });

    const selectedEqual = this.getSelected().every((id) => other.isSelected(id));

    return itemsEqual && selectedEqual;
  }

  clone(): Filter {
    return new Filter({
      id: this.id,
      label: this.label,
      items: this.items.map((item) => ({ ...item })),
      selectionMode: someOrNone(this.selectionMode),
      itemTemplateRef: this.itemTemplateRef,
      selected: new Set(this.getSelected()),
    });
  }
}

export class FilterEvent {
  readonly filters: Filter[];

  private constructor(props: { filters: Filter[] }) {
    this.filters = props.filters;
  }

  static of(props: { filters: Filter[] }): FilterEvent {
    return new FilterEvent({
      filters: props.filters,
    });
  }
}

export type SortingOptionId = string;

export class SortingOption {
  readonly id: SortingOptionId;
  readonly label: string;
  readonly ascendingLabel: string;
  readonly descendingLabel: string;

  private constructor(props: { id: string; label: string; ascendingLabel: string; descendingLabel: string }) {
    this.id = props.id;
    this.label = props.label;
    this.ascendingLabel = props.ascendingLabel;
    this.descendingLabel = props.descendingLabel;
  }

  static of(props: { id: string; label: string; ascendingLabel: string; descendingLabel: string }): SortingOption {
    return new SortingOption({
      id: props.id,
      label: props.label,
      ascendingLabel: props.ascendingLabel,
      descendingLabel: props.descendingLabel,
    });
  }
}

class SortingOptionDropdownItem implements DropdownItem {
  readonly id: SortingOptionId;
  readonly label: string;

  private constructor(props: { id: SortingOptionId; label: string }) {
    this.id = props.id;
    this.label = props.label;
  }

  static fromSortingOption(item: SortingOption): SortingOptionDropdownItem {
    return new SortingOptionDropdownItem({
      id: item.id,
      label: item.label,
    });
  }
}

export class SortEvent {
  readonly option: SortingOption;
  readonly ascending: boolean;

  private constructor(props: { option: SortingOption; ascending: boolean }) {
    this.option = props.option;
    this.ascending = props.ascending;
  }

  static of(props: { option: SortingOption; ascending: boolean }): SortEvent {
    return new SortEvent({
      option: props.option,
      ascending: props.ascending,
    });
  }
}

@Component({
  selector: 'app-filter-sort-bar',
  templateUrl: './filter-sort-bar.component.html',
  styleUrls: ['./filter-sort-bar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class FilterSortBarComponent implements OnInit, OnDestroy {
  @Input('filters')
  set setFilters(filters: Filter[] | null) {
    someOrNone(filters).ifSome((filters) => this.filters$.next(filters));
  }

  @Input('sortingOptions')
  set setSortingOptions(sortingOptions: SortingOption[] | null) {
    someOrNone(sortingOptions).ifSome((sortingOptions) => {
      this.sortingOptions$.next(sortingOptions);
      this.sortBy$.next(sortingOptions[0].id);
    });
  }

  @Output()
  filtered: EventEmitter<FilterEvent> = new EventEmitter<FilterEvent>();

  @Output()
  sorted: EventEmitter<SortEvent> = new EventEmitter<SortEvent>();

  private readonly filters$: Subject<Filter[]> = new ReplaySubject<Filter[]>(1);
  private readonly sortingOptions$: Subject<SortingOption[]> = new ReplaySubject<SortingOption[]>(1);
  private readonly sortBy$: Subject<SortingOptionId> = new ReplaySubject<SortingOptionId>(1);
  private readonly ascending$: Subject<boolean> = new BehaviorSubject<boolean>(true);
  private readonly filtersChanged$: Subject<void> = new Subject<void>();
  private readonly destroy$: Subject<void> = new Subject<void>();

  ngOnInit(): void {
    this.filtersChanged$.pipe(takeUntil(this.destroy$)).subscribe(() => this.emitFilteredEvent());

    combineLatest([this.sortBy$, this.ascending$])
      .pipe(distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(() => this.emitSortedEvent());
  }

  ngOnDestroy(): void {
    this.filters$.complete();
    this.sortingOptions$.complete();
    this.sortBy$.complete();
    this.ascending$.complete();
    this.filtersChanged$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getFilters(): Observable<Filter[]> {
    return this.filters$.asObservable();
  }

  getSortingOptions(): Observable<SortingOption[]> {
    return this.sortingOptions$.asObservable();
  }

  getSelectedSortingOption(): Observable<SortingOptionId> {
    return this.sortBy$.asObservable();
  }

  protected filterToDropdownItems(items: FilterItem[]): DropdownItem[] {
    return items.map((item) => this.filterToDropdownItem(item));
  }

  protected updateFilterDropdownItemsSelection(filter: Filter, dropdownItemIds: DropdownItemId[]): void {
    const filterItemIds = dropdownItemIds.map((id) => id as FilterItemId);
    filter.setSelected(filterItemIds);

    this.filtersChanged$.next();
  }

  protected updateSortingOptionDropdownItemSelection(dropdownItemIds: DropdownItemId[]): void {
    const selectedSortingOptionId = dropdownItemIds[0] as SortingOptionId;
    this.sortBy$.next(selectedSortingOptionId);
  }

  protected updateSortDirectionDropdownItemSelection(dropdownItemIds: DropdownItemId[]): void {
    const selectedSortDirection = dropdownItemIds[0];
    const isAscending = selectedSortDirection === 'ascending';
    this.ascending$.next(isAscending);
  }

  private emitSortedEvent(): void {
    combineLatest([this.sortingOptions$, this.sortBy$, this.ascending$])
      .pipe(take(1))
      .subscribe(([sortingOptions, selectedSortingOptionId, ascending]) => {
        const selectedSortingOption = sortingOptions.find((option) => option.id === selectedSortingOptionId);

        if (!!selectedSortingOption) {
          this.sorted.emit(
            SortEvent.of({
              option: selectedSortingOption,
              ascending,
            }),
          );
        }
      });
  }

  private emitFilteredEvent(): void {
    this.filters$.pipe(take(1)).subscribe((filters) => {
      const activeFilters = filters.filter((filter) => filter.isActive()).map((filter) => filter.clone());
      this.filtered.emit(FilterEvent.of({ filters: activeFilters }));
    });
  }

  private filterToDropdownItem(item: FilterItem): DropdownItem {
    return FilterDropdownItem.fromFilterItem(item);
  }

  protected sortingOptionsToDropdownItems(sortingOptions: SortingOption[]): DropdownItem[] {
    return sortingOptions.map((sortingOption) => this.sortingOptionToDropdownItem(sortingOption));
  }

  private sortingOptionToDropdownItem(sortingOption: SortingOption): DropdownItem {
    return SortingOptionDropdownItem.fromSortingOption(sortingOption);
  }

  protected sortDirectionToDropdownItems(selectedSortingOption: SortingOption): DropdownItem[] {
    return [
      {
        id: 'ascending',
        label: selectedSortingOption.ascendingLabel,
      },
      {
        id: 'descending',
        label: selectedSortingOption.descendingLabel,
      },
    ];
  }

  protected getSortingOption(selectedSortingOption: SortingOptionId): Observable<SortingOption> {
    return this.sortingOptions$.pipe(
      map((sortingOptions) => sortingOptions.find((option) => option.id === selectedSortingOption)),
      filter((option) => !!option),
      map((option) => option as SortingOption),
    );
  }
}
