import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
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
import { COLORS, Fabric, THEMES } from '../../model';
import { FabricsStoreService } from '../../services';

interface Sorting {
  property: string;
  ascending: boolean;
}

@Component({
  selector: 'app-fabrics-page',
  templateUrl: './fabrics.page.html',
  styleUrls: ['./fabrics.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FabricsPage implements OnInit, OnDestroy {
  private readonly activeFilters$: BehaviorSubject<Filter[]> =
    new BehaviorSubject<Filter[]>([]);
  private readonly sorting$: BehaviorSubject<Sorting> =
    new BehaviorSubject<Sorting>({
      property: 'name',
      ascending: true,
    });
  private readonly destroy$: Subject<void> = new Subject<void>();
  protected readonly items$: Subject<CardListItem[]> = new ReplaySubject<
    CardListItem[]
  >(1);

  protected readonly filters: Filter[] = [
    Filter.of({
      id: 'theme',
      label: 'Thema',
      items: THEMES.map((theme) => ({
        id: theme.id,
        label: theme.name,
      })),
      selectionMode: FilterSelectionMode.MULTIPLE,
    }),
    Filter.of({
      id: 'color',
      label: 'Farbe',
      items: COLORS.map((color) => ({
        id: color.id,
        label: color.name,
      })),
      selectionMode: FilterSelectionMode.MULTIPLE,
    }),
    Filter.of({
      id: 'availability',
      label: 'Verfügbarkeit',
      items: [
        { id: 'available', label: 'Auf Lager' },
        { id: 'unavailable', label: 'Nicht auf Lager' },
      ],
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

  constructor(private readonly fabricsStore: FabricsStoreService) {}

  ngOnDestroy(): void {
    this.sorting$.complete();
    this.items$.complete();
    this.activeFilters$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnInit(): void {
    combineLatest([
      this.fabricsStore.getFabrics(),
      this.sorting$,
      this.activeFilters$,
    ])
      .pipe(
        map(([fabrics, sorting, filters]) =>
          this.sortItems(this.filterItems(fabrics, filters), sorting).map(
            (fabric) => this.mapFabricToItem(fabric),
          ),
        ),
        takeUntil(this.destroy$),
      )
      .subscribe((items) => this.items$.next(items));
  }

  protected updateFilters(filters: Filter[]): void {
    this.activeFilters$.next(filters);
  }

  private mapFabricToItem(fabric: Fabric): CardListItem {
    return CardListItem.of({
      title: fabric.name,
      description: fabric.getStockStatusLabel(),
      link: `/catalog/fabrics/${fabric.id}`,
      imageUrl: fabric.image.url ?? '',
    });
  }

  protected updateSorting(option: SortingOption, ascending: boolean): void {
    this.sorting$.next({
      property: option.id,
      ascending,
    });
  }

  private filterItems(fabrics: Fabric[], filters: Filter[]): Fabric[] {
    return fabrics.filter((fabric) => this.matchesFilters(fabric, filters));
  }

  private matchesFilters(fabric: Fabric, filters: Filter[]): boolean {
    return filters.every((filter) => this.matchesFilter(fabric, filter));
  }

  private matchesFilter(fabric: Fabric, filter: Filter): boolean {
    const filterId = filter.id;
    switch (filterId) {
      case 'theme':
        return this.matchesThemeFilter(fabric, filter);
      case 'color':
        return this.matchesColorFilter(fabric, filter);
      case 'availability':
        return this.matchesAvailabilityFilter(fabric, filter);
      default:
        throw new Error(`Unknown filter id: ${filterId}`);
    }
  }

  private matchesThemeFilter(fabric: Fabric, filter: Filter): boolean {
    return [...fabric.themes]
      .map((theme) => theme.id)
      .some((id) => filter.isSelected(id));
  }

  private matchesColorFilter(fabric: Fabric, filter: Filter): boolean {
    return [...fabric.colors]
      .map((color) => color.id)
      .some((id) => filter.isSelected(id));
  }

  private matchesAvailabilityFilter(fabric: Fabric, filter: Filter): boolean {
    if (filter.isSelected('available')) {
      return fabric.availability.isAvailableInAnyType();
    } else if (filter.isSelected('unavailable')) {
      return !fabric.availability.isAvailableInAnyType();
    }

    return true;
  }

  private sortItems(fabrics: Fabric[], sorting: Sorting): Fabric[] {
    const result = [...fabrics].sort((a, b) => {
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

  private compareByName(a: Fabric, b: Fabric): number {
    return a.name.localeCompare(b.name, 'de-de', {
      sensitivity: 'base',
      numeric: true,
    });
  }
}
