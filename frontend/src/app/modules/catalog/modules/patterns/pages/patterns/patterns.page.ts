import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { PatternsStoreService } from '../../services';
import {
  BehaviorSubject,
  combineLatest,
  map,
  ReplaySubject,
  Subject,
  takeUntil,
} from 'rxjs';
import {
  CardListItem,
  Filter,
  FilterSelectionMode,
  SortingOption,
} from '../../../../../shared';
import { Pattern } from '../../model';
import { CATEGORIES } from '../../model/category';

interface Sorting {
  property: string;
  ascending: boolean;
}

@Component({
  selector: 'app-patterns-page',
  templateUrl: './patterns.page.html',
  styleUrls: ['./patterns.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternsPage implements OnInit, OnDestroy {
  protected readonly items$: Subject<CardListItem[]> = new ReplaySubject<
    CardListItem[]
  >(1);

  private readonly filterSizeRange = {
    from: 50,
    to: 116,
  };
  private readonly filterSizes = Array.from(
    { length: (this.filterSizeRange.to - this.filterSizeRange.from) / 6 + 1 },
    (_, i) => this.filterSizeRange.from + i * 6,
  );

  protected readonly filters: Filter[] = [
    Filter.of({
      id: 'category',
      label: 'Kategorie',
      items: CATEGORIES.map((category) => ({
        id: category.id,
        label: category.name,
      })),
      selectionMode: FilterSelectionMode.MULTIPLE,
    }),
    Filter.of({
      id: 'size',
      label: 'Größe',
      items: this.filterSizes.map((size) => ({
        id: size.toString(10),
        label: size.toString(10),
      })),
      selectionMode: FilterSelectionMode.MULTIPLE,
    }),
  ];

  protected readonly sortingOptions: SortingOption[] = [
    SortingOption.of({
      id: 'name',
      label: 'Name',
      ascendingLabel: 'A-Z',
      descendingLabel: 'Z-A',
    }),
  ];

  private readonly activeFilters$: BehaviorSubject<Filter[]> =
    new BehaviorSubject<Filter[]>([]);
  private readonly sorting$: BehaviorSubject<Sorting> =
    new BehaviorSubject<Sorting>({
      property: 'name',
      ascending: true,
    });
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(private readonly patternsStore: PatternsStoreService) {}

  ngOnInit(): void {
    combineLatest([
      this.patternsStore.getPatterns(),
      this.sorting$,
      this.activeFilters$,
    ])
      .pipe(
        map(([patterns, sorting, filters]) =>
          this.sortItems(this.filterItems(patterns, filters), sorting).map(
            (pattern) => this.mapPatternToItem(pattern),
          ),
        ),
        takeUntil(this.destroy$),
      )
      .subscribe((items) => this.items$.next(items));
  }

  ngOnDestroy(): void {
    this.sorting$.complete();
    this.items$.complete();
    this.activeFilters$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  protected updateFilters(filters: Filter[]): void {
    this.activeFilters$.next(filters);
  }

  protected updateSorting(option: SortingOption, ascending: boolean): void {
    this.sorting$.next({
      property: option.id,
      ascending,
    });
  }

  private mapPatternToItem(pattern: Pattern): CardListItem {
    return CardListItem.of({
      title: pattern.name,
      description: `ab ${pattern.getStartingPrice().formatted()}, Größe ${pattern.getFormattedSizeRange()}`,
      link: `/catalog/patterns/${pattern.id}`,
      imageUrl: pattern.previewImage.url ?? '',
    });
  }

  private sortItems(patterns: Pattern[], sorting: Sorting): Pattern[] {
    const result = [...patterns].sort((a, b) => {
      const property = sorting.property;
      if (property === 'name') {
        return this.compareByName(a, b);
      } else {
        throw new Error(`Unknown sort property: ${property}`);
      }
    });

    if (!sorting.ascending) {
      result.reverse();
    }

    return result;
  }

  private compareByName(a: Pattern, b: Pattern): number {
    return a.name.localeCompare(b.name, 'de-de', {
      sensitivity: 'base',
      numeric: true,
    });
  }

  private filterItems(patterns: Pattern[], filters: Filter[]): Pattern[] {
    return patterns.filter((pattern) => this.matchesFilters(pattern, filters));
  }

  private matchesFilters(pattern: Pattern, filters: Filter[]): boolean {
    return filters.every((filter) => this.matchesFilter(pattern, filter));
  }

  private matchesFilter(pattern: Pattern, filter: Filter): boolean {
    const filterId = filter.id;
    switch (filterId) {
      case 'category':
        return this.matchesCategoryFilter(pattern, filter);
      case 'size':
        return this.matchesSizeFilter(pattern, filter);
      default:
        throw new Error(`Unknown filter id: ${filterId}`);
    }
  }

  private matchesCategoryFilter(pattern: Pattern, filter: Filter): boolean {
    return [...pattern.categories]
      .map((theme) => theme.id)
      .some((id) => filter.isSelected(id));
  }

  private matchesSizeFilter(pattern: Pattern, filter: Filter): boolean {
    const sizes = filter.getSelected().map((size) => parseInt(size, 10));
    return sizes.some((size) => pattern.isAvailableInSize(size));
  }
}
