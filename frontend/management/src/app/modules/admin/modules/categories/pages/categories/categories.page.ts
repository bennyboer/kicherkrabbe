import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  debounceTime,
  EMPTY,
  finalize,
  first,
  map,
  Observable,
  Subject,
  takeUntil,
  tap,
} from 'rxjs';
import { DropdownComponent, DropdownItem, DropdownItemId, NotificationService } from '../../../../../shared';
import { Category, CategoryGroup, CategoryGroupType, CLOTHING, GROUPS } from '../../model';
import { CategoriesService } from '../../services';
import { none, Option, someOrNone } from '@kicherkrabbe/shared';

@Component({
  selector: 'app-categories-page',
  templateUrl: './categories.page.html',
  styleUrls: ['./categories.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class CategoriesPage implements OnInit, OnDestroy {
  private readonly groups$: BehaviorSubject<CategoryGroup[]> = new BehaviorSubject<CategoryGroup[]>(GROUPS);
  private readonly selectedGroup$: BehaviorSubject<Option<CategoryGroup>> = new BehaviorSubject<Option<CategoryGroup>>(
    none(),
  );
  protected readonly groupDropdownItems$: Observable<DropdownItem[]> = this.groups$.pipe(
    map((groups) => groups.map((g) => ({ id: g.type, label: g.name }))),
  );

  private readonly searchTerm$: BehaviorSubject<string> = new BehaviorSubject<string>('');

  protected readonly categories$: BehaviorSubject<Category[]> = new BehaviorSubject<Category[]>([]);
  private readonly loadingCategories$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);

  protected readonly loading$: Observable<boolean> = this.loadingCategories$;
  protected readonly notLoading$: Observable<boolean> = this.loadingCategories$.pipe(map((loading) => !loading));

  private readonly destroy$: Subject<void> = new Subject<void>();

  private listeningToChanges: boolean = false;

  constructor(
    private readonly categoriesService: CategoriesService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    combineLatest([this.searchTerm$.pipe(debounceTime(300)), this.selectedGroup$])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([searchTerm, group]) => this.reloadCategories({ searchTerm, group: group.orElseNull() }));
  }

  ngOnDestroy(): void {
    this.groups$.complete();
    this.selectedGroup$.complete();
    this.searchTerm$.complete();
    this.categories$.complete();
    this.loadingCategories$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateSearchTerm(value: string): void {
    this.searchTerm$.next(value);
  }

  updateSelectedGroup(dropdown: DropdownComponent, items: DropdownItemId[]): void {
    if (items.length === 0) {
      this.selectedGroup$.next(none());
      return;
    }

    if (items.length > 1) {
      throw new Error('Only one group can be selected');
    }

    const item = items[0];
    const groupType: CategoryGroupType = this.dropdownItemToCategoryGroupType(item);
    const group: Option<CategoryGroup> = someOrNone(this.groups$.value.find((g) => g.type === groupType));
    this.selectedGroup$.next(group);

    dropdown.toggleOpened();
  }

  clearGroupSelection(dropdown: DropdownComponent): void {
    dropdown.clearSelection();
    dropdown.toggleOpened();
  }

  private dropdownItemToCategoryGroupType(item: DropdownItemId): CategoryGroupType {
    switch (item) {
      case 'CLOTHING':
        return CategoryGroupType.CLOTHING;
      case 'NONE':
        return CategoryGroupType.NONE;
      default:
        throw new Error(`Unknown group type: ${item}`);
    }
  }

  private reloadCategories(props: {
    searchTerm?: string;
    group?: CategoryGroup | null;
    indicateLoading?: boolean;
  }): void {
    const indicateLoading = someOrNone(props.indicateLoading).orElse(true);
    if (indicateLoading) {
      this.loadingCategories$.next(true);
    }

    this.loadCategories(props)
      .pipe(
        first(),
        finalize(() => this.loadingCategories$.next(false)),
      )
      .subscribe((categories) => this.categories$.next(categories));
  }

  private loadCategories(props: { searchTerm?: string; group?: CategoryGroup | null }): Observable<Category[]> {
    return this.categoriesService
      .getCategories({
        searchTerm: props.searchTerm,
        group: props.group,
      })
      .pipe(
        tap(() => this.listenToChangesIfNotAlreadyListening()),
        catchError((e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Kategorien konnten nicht geladen werden. Bitte versuche die Seite neu zu laden.',
          });
          return EMPTY;
        }),
      );
  }

  private listenToChangesIfNotAlreadyListening() {
    if (this.listeningToChanges) {
      return;
    }
    this.listeningToChanges = true;

    this.categoriesService
      .getCategoryChanges()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.reloadCategories({
          searchTerm: this.searchTerm$.value,
          group: this.selectedGroup$.value.orElseNull(),
          indicateLoading: false,
        });
      });
  }
}
