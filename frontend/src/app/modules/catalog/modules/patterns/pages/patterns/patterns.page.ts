import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  distinctUntilChanged,
  EMPTY,
  filter,
  finalize,
  first,
  map,
  Observable,
  Subject,
  takeUntil,
} from 'rxjs';
import {
  CardListItem,
  Filter,
  FilterSelectionMode,
  NotificationService,
  SortingOption,
} from '../../../../../shared';
import { Category, ImageId, Pattern } from '../../model';
import { PatternCategoriesService, PatternsService } from '../../services';
import { environment } from '../../../../../../../environments';
import { Eq, validateProps } from '../../../../../../util';

class Sorting implements Eq<Sorting> {
  readonly property: string;
  readonly ascending: boolean;

  private constructor(props: { property: string; ascending: boolean }) {
    validateProps(props);

    this.property = props.property;
    this.ascending = props.ascending;
  }

  static of(props: { property: string; ascending: boolean }): Sorting {
    return new Sorting({
      property: props.property,
      ascending: props.ascending,
    });
  }

  equals(other: Sorting): boolean {
    return (
      this.property === other.property && this.ascending === other.ascending
    );
  }
}

@Component({
  selector: 'app-patterns-page',
  templateUrl: './patterns.page.html',
  styleUrls: ['./patterns.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternsPage implements OnInit, OnDestroy {
  protected readonly patterns$: BehaviorSubject<Pattern[]> =
    new BehaviorSubject<Pattern[]>([]);
  protected readonly patternsLoading$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);

  protected readonly categories$: BehaviorSubject<Category[]> =
    new BehaviorSubject<Category[]>([]);
  protected readonly categoriesLoading$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);

  protected readonly loading$: Observable<boolean> = combineLatest([
    this.patternsLoading$,
    this.categoriesLoading$,
  ]).pipe(
    map(
      ([patternsLoading, categoriesLoading]) =>
        patternsLoading || categoriesLoading,
    ),
  );
  protected readonly notLoading$: Observable<boolean> = this.loading$.pipe(
    map((loading) => !loading),
  );
  protected readonly items$: Observable<CardListItem[]> = this.patterns$.pipe(
    map((patterns) =>
      patterns.map((pattern) => this.mapPatternToItem(pattern)),
    ),
  );

  private readonly filterSizeRange = {
    from: 50,
    to: 116,
  };
  private readonly filterSizes = Array.from(
    { length: (this.filterSizeRange.to - this.filterSizeRange.from) / 6 + 1 },
    (_, i) => this.filterSizeRange.from + i * 6,
  );

  protected readonly sortingOptions: SortingOption[] = [
    SortingOption.of({
      id: 'name',
      label: 'Name',
      ascendingLabel: 'A-Z',
      descendingLabel: 'Z-A',
    }),
  ];

  protected readonly filters$: Observable<Filter[]> = this.categories$.pipe(
    filter((categories) => categories.length > 0),
    map((categories) => {
      const categoryFilter = Filter.of({
        id: 'category',
        label: 'Kategorie',
        items: categories.map((category) => ({
          id: category.id,
          label: category.name,
        })),
        selectionMode: FilterSelectionMode.MULTIPLE,
      });

      const sizeFilter = Filter.of({
        id: 'size',
        label: 'Größe',
        items: this.filterSizes.map((size) => ({
          id: size.toString(10),
          label: size.toString(10),
        })),
        selectionMode: FilterSelectionMode.MULTIPLE,
      });

      return [categoryFilter, sizeFilter];
    }),
  );

  private readonly activeFilters$: BehaviorSubject<Filter[]> =
    new BehaviorSubject<Filter[]>([]);
  private readonly sorting$: BehaviorSubject<Sorting> =
    new BehaviorSubject<Sorting>(
      Sorting.of({
        property: 'name',
        ascending: true,
      }),
    );
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly patternsService: PatternsService,
    private readonly patternCategoriesService: PatternCategoriesService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.reloadCategories();

    combineLatest([this.sorting$, this.activeFilters$])
      .pipe(
        distinctUntilChanged(([sortingA, filtersA], [sortingB, filtersB]) => {
          const sortingEqual = sortingA.equals(sortingB);
          const filtersEqual =
            filtersA.length === filtersB.length &&
            filtersA.every((filter, index) => filter.equals(filtersB[index]));

          return sortingEqual && filtersEqual;
        }),
        takeUntil(this.destroy$),
      )
      .subscribe(([sorting, filters]) => this.reloadPatterns(sorting, filters));
  }

  ngOnDestroy(): void {
    this.patterns$.complete();
    this.patternsLoading$.complete();

    this.categories$.complete();
    this.categoriesLoading$.complete();

    this.sorting$.complete();
    this.activeFilters$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  protected updateFilters(filters: Filter[]): void {
    this.activeFilters$.next(filters);
  }

  protected updateSorting(option: SortingOption, ascending: boolean): void {
    this.sorting$.next(
      Sorting.of({
        property: option.id,
        ascending,
      }),
    );
  }

  private mapPatternToItem(pattern: Pattern): CardListItem {
    return CardListItem.of({
      title: pattern.name,
      description: `ab ${pattern.getStartingPrice().formatted()}, Größe ${pattern.getFormattedSizeRange()}`,
      link: `/catalog/patterns/${pattern.alias}`,
      imageUrl: this.getImageUrl(pattern.images[0]),
    });
  }

  private getImageUrl(imageId: ImageId): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  private reloadPatterns(sorting: Sorting, filters: Filter[]): void {
    this.patternsLoading$.next(true);

    const categoriesFilter = filters.find((filter) => filter.id === 'category');
    const sizesFilter = filters.find((filter) => filter.id === 'size');

    const categories: Set<string> = new Set<string>(
      categoriesFilter?.getSelected(),
    );
    const sizes: Set<number> = new Set<number>(
      sizesFilter?.getSelected().map((size) => parseInt(size, 10)),
    );

    this.patternsService
      .getPatterns({
        categories,
        sizes,
        ascending: sorting.ascending,
        limit: 9999,
      })
      .pipe(
        first(),
        catchError((e) => {
          this.notificationService.publish({
            type: 'error',
            message:
              'Die Schnitte konnten nicht geladen werden. Bitte versuchen Sie es später erneut.',
          });
          return EMPTY;
        }),
        finalize(() => this.patternsLoading$.next(false)),
        takeUntil(this.destroy$),
      )
      .subscribe((patterns) => this.patterns$.next(patterns));
  }

  private reloadCategories(): void {
    this.categoriesLoading$.next(true);

    this.patternCategoriesService
      .getCategories()
      .pipe(
        first(),
        catchError((e) => {
          this.notificationService.publish({
            type: 'error',
            message:
              'Die Kategorien konnten nicht geladen werden. Bitte versuchen Sie es später erneut.',
          });
          return EMPTY;
        }),
        finalize(() => this.categoriesLoading$.next(false)),
        takeUntil(this.destroy$),
      )
      .subscribe((categories) => this.categories$.next(categories));
  }
}
