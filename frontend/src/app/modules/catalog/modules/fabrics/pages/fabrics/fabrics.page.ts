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
  Observable,
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
import { Fabric } from '../../model';
import { FabricsStoreService } from '../../services';
import { none, Option, some } from '../../../../../../util';
import { FabricsFilter } from '../../services/store.service';

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

  protected readonly filters$: Observable<Filter[]> = combineLatest([
    this.fabricsStore.getAvailableThemes(),
    this.fabricsStore.getAvailableColors(),
  ]).pipe(
    map(([themes, colors]) => {
      const availability = Filter.of({
        id: 'availability',
        label: 'Verfügbarkeit',
        items: [
          { id: 'available', label: 'Auf Lager' },
          { id: 'unavailable', label: 'Nicht auf Lager' },
        ],
      });

      const theme = Filter.of({
        id: 'theme',
        label: 'Thema',
        items: themes.map((theme) => ({
          id: theme.id,
          label: theme.name,
        })),
        selectionMode: FilterSelectionMode.MULTIPLE,
      });

      const color = Filter.of({
        id: 'color',
        label: 'Farbe',
        items: colors.map((color) => ({
          id: color.id,
          label: color.name,
        })),
        selectionMode: FilterSelectionMode.MULTIPLE,
      });

      return [theme, color, availability];
    }),
  );

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
    this.activeFilters$
      .pipe(
        map((filters) => {
          const themeFilter = filters.find((filter) => filter.id === 'theme');
          const colorFilter = filters.find((filter) => filter.id === 'color');
          const availabilityFilter = filters.find(
            (filter) => filter.id === 'availability',
          );

          const themeIds: Option<Set<string>> = themeFilter?.isActive()
            ? some(new Set<string>(themeFilter.getSelected()))
            : none();
          const colorIds: Option<Set<string>> = colorFilter?.isActive()
            ? some(new Set<string>(colorFilter.getSelected()))
            : none();
          const inStock: Option<boolean> = availabilityFilter?.isActive()
            ? some(availabilityFilter.isSelected('available'))
            : none();

          return FabricsFilter.of({
            themeIds,
            colorIds,
            inStock,
          });
        }),
        takeUntil(this.destroy$),
      )
      .subscribe((filter) => this.fabricsStore.updateFilter(filter));

    combineLatest([this.fabricsStore.getFabrics(), this.sorting$])
      .pipe(
        map(([fabrics, sorting]) =>
          this.sortItems(fabrics, sorting).map((fabric) =>
            this.mapFabricToItem(fabric),
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
