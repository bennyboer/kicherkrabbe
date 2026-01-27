import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import {
  BehaviorSubject,
  combineLatest,
  debounceTime,
  filter,
  map,
  Observable,
  ReplaySubject,
  Subject,
  takeUntil,
  tap,
} from 'rxjs';
import { CardListItem, Filter, FilterSelectionMode, SortingOption } from '../../../../../shared';
import { Color, Fabric, Theme } from '../../model';
import { RemoteFabricsService } from '../../services';
import { none, Option, some } from '../../../../../shared/modules/option';

interface Sorting {
  property: string;
  ascending: boolean;
}

export class FabricsFilter {
  readonly colorIds: Option<Set<string>>;
  readonly themeIds: Option<Set<string>>;
  readonly inStock: Option<boolean>;

  private constructor(props: {
    colorIds: Option<Set<string>>;
    themeIds: Option<Set<string>>;
    inStock: Option<boolean>;
  }) {
    this.colorIds = props.colorIds;
    this.themeIds = props.themeIds;
    this.inStock = props.inStock;
  }

  static of(props: {
    colorIds: Option<Set<string>>;
    themeIds: Option<Set<string>>;
    inStock: Option<boolean>;
  }): FabricsFilter {
    return new FabricsFilter({
      colorIds: props.colorIds,
      themeIds: props.themeIds,
      inStock: props.inStock,
    });
  }

  static empty(): FabricsFilter {
    return new FabricsFilter({
      colorIds: none(),
      themeIds: none(),
      inStock: none(),
    });
  }

  equals(other: FabricsFilter): boolean {
    return (
      this.colorIds.equals(other.colorIds) && this.themeIds.equals(other.themeIds) && this.inStock.equals(other.inStock)
    );
  }
}

const FABRICS_LIMIT = 50;

@Component({
    selector: 'app-fabrics-page',
    templateUrl: './fabrics.page.html',
    styleUrls: ['./fabrics.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class FabricsPage implements OnInit, OnDestroy {
  @ViewChild('colorItemTemplate')
  private readonly colorItemTemplate!: TemplateRef<any>;

  private readonly availableThemes$: BehaviorSubject<Set<Theme>> = new BehaviorSubject<Set<Theme>>(new Set<Theme>());
  private readonly availableColors$: BehaviorSubject<Set<Color>> = new BehaviorSubject<Set<Color>>(new Set<Color>());
  private readonly activeFilters$: BehaviorSubject<FabricsFilter> = new BehaviorSubject<FabricsFilter>(
    FabricsFilter.empty(),
  );
  private readonly sorting$: BehaviorSubject<Sorting> = new BehaviorSubject<Sorting>({
    property: 'name',
    ascending: true,
  });
  private readonly fabrics$: BehaviorSubject<Fabric[]> = new BehaviorSubject<Fabric[]>([]);
  private readonly loadingFabrics$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
  protected readonly items$: Subject<CardListItem[]> = new ReplaySubject<CardListItem[]>(1);
  private readonly destroy$: Subject<void> = new Subject<void>();

  protected readonly loading$: Observable<boolean> = this.loadingFabrics$.asObservable();
  protected readonly notLoading$: Observable<boolean> = this.loading$.pipe(map((loading) => !loading));
  protected readonly filters$: Observable<Filter[]> = combineLatest([
    this.availableThemes$,
    this.availableColors$,
  ]).pipe(
    filter(([themes, colors]) => themes.size > 0 && colors.size > 0),
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
        items: Array.from(themes)
          .sort((a, b) => a.name.localeCompare(b.name))
          .map((theme) => ({
            id: theme.id,
            label: theme.name,
          })),
        selectionMode: FilterSelectionMode.MULTIPLE,
      });

      const color = Filter.of({
        id: 'color',
        label: 'Farbe',
        items: Array.from(colors)
          .sort((a, b) => a.name.localeCompare(b.name))
          .map((color) => ({
            id: color.id,
            label: color.name,
            color: {
              red: color.red,
              green: color.green,
              blue: color.blue,
            },
          })),
        selectionMode: FilterSelectionMode.MULTIPLE,
        itemTemplateRef: this.colorItemTemplate,
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

  constructor(private readonly fabricsService: RemoteFabricsService) {}

  ngOnInit(): void {
    this.reloadAvailableThemes();
    this.reloadAvailableColors();

    combineLatest([this.activeFilters$, this.sorting$])
      .pipe(debounceTime(100), takeUntil(this.destroy$))
      .subscribe(([filters, sorting]) => this.reloadFabrics(filters, sorting));

    this.fabrics$.pipe(takeUntil(this.destroy$)).subscribe((fabrics) => {
      const items = fabrics.map((fabric) => this.mapFabricToItem(fabric));

      this.items$.next(items);
    });
  }

  ngOnDestroy(): void {
    this.sorting$.complete();
    this.items$.complete();
    this.activeFilters$.complete();
    this.availableColors$.complete();
    this.availableThemes$.complete();
    this.fabrics$.complete();
    this.loadingFabrics$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  onScroll(): void {
    const skip = this.fabrics$.value.length;

    this.loadFabrics(this.activeFilters$.value, this.sorting$.value, skip, FABRICS_LIMIT)
      .pipe(takeUntil(this.destroy$))
      .subscribe((fabrics) => {
        const currentFabrics = this.fabrics$.value;
        this.fabrics$.next([...currentFabrics, ...fabrics]);
      });
  }

  protected updateFilters(filters: Filter[]): void {
    const themeFilter = filters.find((filter) => filter.id === 'theme');
    const colorFilter = filters.find((filter) => filter.id === 'color');
    const availabilityFilter = filters.find((filter) => filter.id === 'availability');

    const themeIds: Option<Set<string>> = themeFilter?.isActive()
      ? some(new Set<string>(themeFilter.getSelected()))
      : none();
    const colorIds: Option<Set<string>> = colorFilter?.isActive()
      ? some(new Set<string>(colorFilter.getSelected()))
      : none();
    const inStock: Option<boolean> = availabilityFilter?.isActive()
      ? some(availabilityFilter.isSelected('available'))
      : none();

    const filter = FabricsFilter.of({
      themeIds,
      colorIds,
      inStock,
    });

    const oldFilter = this.activeFilters$.value;
    if (oldFilter.equals(filter)) {
      return;
    }

    this.activeFilters$.next(filter);
  }

  private mapFabricToItem(fabric: Fabric): CardListItem {
    return CardListItem.of({
      title: fabric.name,
      link: `/catalog/fabrics/${fabric.id}`,
      imageUrl: fabric.image.url ?? '',
      content: {
        inStock: fabric.isAvailableInAnyType(),
      },
    });
  }

  protected updateSorting(option: SortingOption, ascending: boolean): void {
    this.sorting$.next({
      property: option.id,
      ascending,
    });
  }

  private reloadAvailableThemes(): void {
    this.fabricsService
      .getAvailableThemes()
      .pipe(takeUntil(this.destroy$))
      .subscribe((themes) => {
        this.availableThemes$.next(new Set<Theme>(themes));
      });
  }

  private reloadAvailableColors(): void {
    this.fabricsService
      .getAvailableColors()
      .pipe(takeUntil(this.destroy$))
      .subscribe((colors) => {
        this.availableColors$.next(new Set<Color>(colors));
      });
  }

  private reloadFabrics(filters: FabricsFilter, sorting: Sorting): void {
    this.fabrics$.next([]);
    this.loadFabrics(filters, sorting, 0, FABRICS_LIMIT)
      .pipe(takeUntil(this.destroy$))
      .subscribe((fabrics) => this.fabrics$.next(fabrics));
  }

  private loadFabrics(filters: FabricsFilter, sorting: Sorting, skip: number, limit: number): Observable<Fabric[]> {
    this.loadingFabrics$.next(true);

    return this.fabricsService
      .getFabrics({
        topicIds: Array.from(filters.themeIds.orElse(new Set<string>())),
        colorIds: Array.from(filters.colorIds.orElse(new Set<string>())),
        availability: {
          active: filters.inStock.isSome(),
          inStock: filters.inStock.orElse(true),
        },
        sort: {
          ascending: sorting.ascending,
        },
        skip,
        limit,
      })
      .pipe(
        tap({
          next: () => this.loadingFabrics$.next(false),
          error: () => this.loadingFabrics$.next(false),
        }),
      );
  }
}
