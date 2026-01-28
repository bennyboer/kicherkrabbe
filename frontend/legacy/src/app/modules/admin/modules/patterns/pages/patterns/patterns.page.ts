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
import { PatternCategoriesService, PatternsService } from '../../services';
import { DropdownComponent, DropdownItem, DropdownItemId, NotificationService } from '../../../../../shared';
import { Pattern, PatternCategory } from '../../model';
import { someOrNone } from '../../../../../shared/modules/option';

@Component({
  selector: 'app-patterns-page',
  templateUrl: './patterns.page.html',
  styleUrls: ['./patterns.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class PatternsPage implements OnInit, OnDestroy {
  protected readonly searchTerm$: BehaviorSubject<string> = new BehaviorSubject<string>('');

  protected readonly usedCategories$: BehaviorSubject<PatternCategory[]> = new BehaviorSubject<PatternCategory[]>([]);
  protected readonly loadingUsedCategories$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
  protected readonly selectedCategories$: BehaviorSubject<Set<string>> = new BehaviorSubject<Set<string>>(
    new Set<string>(),
  );
  protected readonly categoriesDropdownItems$: Observable<DropdownItem[]> = this.usedCategories$.pipe(
    map((categories) => categories.map((g) => ({ id: g.id, label: g.name }))),
  );
  protected readonly categoryLabelLookup$: Observable<Map<string, string>> = this.usedCategories$.pipe(
    map((categories) => new Map(categories.map((c) => [c.id, c.name]))),
  );

  protected readonly patterns$: BehaviorSubject<Pattern[]> = new BehaviorSubject<Pattern[]>([]);
  private readonly loadingPatterns$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);

  protected readonly loading$: Observable<boolean> = combineLatest([
    this.loadingUsedCategories$,
    this.loadingPatterns$,
  ]).pipe(map(([loadingUsedCategories, loadingPatterns]) => loadingUsedCategories || loadingPatterns));
  protected readonly notLoading$: Observable<boolean> = this.loading$.pipe(map((loading) => !loading));

  private readonly destroy$: Subject<void> = new Subject<void>();

  private listeningToChanges: boolean = false;

  constructor(
    private readonly patternsService: PatternsService,
    private readonly patternCategoriesService: PatternCategoriesService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.reloadUsedCategories();

    combineLatest([this.searchTerm$.pipe(debounceTime(300)), this.selectedCategories$])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([searchTerm, categories]) => this.reloadPatterns({ searchTerm, categories }));
  }

  ngOnDestroy(): void {
    this.searchTerm$.complete();
    this.patterns$.complete();
    this.loadingPatterns$.complete();
    this.selectedCategories$.complete();
    this.usedCategories$.complete();
    this.loadingUsedCategories$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateSearch(value: string): void {
    this.searchTerm$.next(value.trim());
  }

  updateSelectedCategories(items: DropdownItemId[]): void {
    if (items.length === 0) {
      this.selectedCategories$.next(new Set<string>());
      return;
    }

    this.selectedCategories$.next(new Set(items));
  }

  clearSelectedCategories(dropdown: DropdownComponent): void {
    dropdown.clearSelection();
    dropdown.toggleOpened();
  }

  getCategoryLabel(id: string): Observable<string> {
    return this.categoryLabelLookup$.pipe(map((lookup) => someOrNone(lookup.get(id)).orElse('X')));
  }

  private reloadPatterns(props: { searchTerm?: string; categories?: Set<string>; indicateLoading?: boolean }): void {
    const indicateLoading = someOrNone(props.indicateLoading).orElse(true);
    if (indicateLoading) {
      this.loadingPatterns$.next(true);
    }

    this.loadPatterns(props)
      .pipe(
        first(),
        finalize(() => this.loadingPatterns$.next(false)),
      )
      .subscribe((categories) => this.patterns$.next(categories));
  }

  private loadPatterns(props: { searchTerm?: string; categories?: Set<string> }): Observable<Pattern[]> {
    const searchTerm = someOrNone(props.searchTerm).orElse('');
    const categories = someOrNone(props.categories).orElse(new Set<string>());

    return this.patternsService
      .getPatterns({
        searchTerm,
        categories: Array.from(categories),
        limit: 99999,
      })
      .pipe(
        tap(() => this.listenToChangesIfNotAlreadyListening()),
        catchError((e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Schnittmuster konnten nicht geladen werden. Bitte versuche die Seite neu zu laden.',
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

    this.patternsService
      .getPatternChanges()
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.reloadPatterns({
          searchTerm: this.searchTerm$.value,
          categories: this.selectedCategories$.value,
          indicateLoading: false,
        });
      });
  }

  private reloadUsedCategories(): void {
    this.loadingUsedCategories$.next(true);

    this.patternCategoriesService
      .getUsedCategories()
      .pipe(
        first(),
        finalize(() => this.loadingUsedCategories$.next(false)),
        catchError((e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Kategorien konnten nicht geladen werden. Bitte versuche die Seite neu zu laden.',
          });
          return EMPTY;
        }),
      )
      .subscribe((categories) => this.usedCategories$.next(categories));
  }
}
