import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import {
  BehaviorSubject,
  combineLatest,
  debounceTime,
  finalize,
  first,
  map,
  Observable,
  Subject,
  takeUntil,
} from 'rxjs';
import { someOrNone } from '@kicherkrabbe/shared';
import { Embroidery, EmbroideryCategory } from '../../model';
import { EmbroideriesService, EmbroideryCategoriesService } from '../../services';
import { DropdownComponent, DropdownItem, DropdownItemId, NotificationService } from '../../../../../shared';
import { environment } from '../../../../../../../environments';

const EMBROIDERIES_LIMIT = 10;

@Component({
  selector: 'app-embroideries-page',
  templateUrl: './embroideries.page.html',
  styleUrls: ['./embroideries.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class EmbroideriesPage implements OnInit, OnDestroy {
  protected readonly embroideriesLoaded$ = new BehaviorSubject<boolean>(false);
  protected readonly loadingEmbroideries$ = new BehaviorSubject<boolean>(false);
  protected readonly embroideries$ = new BehaviorSubject<Embroidery[]>([]);
  protected readonly totalEmbroideries$ = new BehaviorSubject<number>(0);

  protected readonly searchValue$ = new BehaviorSubject<string>('');
  protected readonly selectedCategories$ = new BehaviorSubject<Set<string>>(new Set<string>());

  protected readonly usedCategories$ = new BehaviorSubject<EmbroideryCategory[]>([]);
  protected readonly categoriesDropdownItems$: Observable<DropdownItem[]> = this.usedCategories$.pipe(
    map((categories) => categories.map((c) => ({ id: c.id, label: c.name }))),
  );
  private readonly categoryLabelLookup$ = this.usedCategories$.pipe(
    map((categories) => new Map(categories.map((c) => [c.id, c.name]))),
  );

  protected readonly loading$ = this.loadingEmbroideries$.asObservable();
  protected readonly remainingEmbroideriesCount$ = combineLatest([this.totalEmbroideries$, this.embroideries$]).pipe(
    map(([total, embroideries]) => total - embroideries.length),
  );
  protected readonly moreEmbroideriesAvailable$ = this.remainingEmbroideriesCount$.pipe(map((count) => count > 0));

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly embroideriesService: EmbroideriesService,
    private readonly embroideryCategoriesService: EmbroideryCategoriesService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.reloadUsedCategories();

    combineLatest([this.searchValue$.pipe(debounceTime(300)), this.selectedCategories$])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([searchTerm, categories]) => this.reloadEmbroideries({ searchTerm, categories }));
  }

  ngOnDestroy(): void {
    this.embroideriesLoaded$.complete();
    this.loadingEmbroideries$.complete();
    this.embroideries$.complete();
    this.totalEmbroideries$.complete();

    this.searchValue$.complete();
    this.selectedCategories$.complete();
    this.usedCategories$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  getCategoryLabel(id: string): Observable<string> {
    return this.categoryLabelLookup$.pipe(map((lookup) => someOrNone(lookup.get(id)).orElse('?')));
  }

  loadMoreEmbroideries(): void {
    this.reloadEmbroideries({
      searchTerm: this.searchValue$.value,
      categories: this.selectedCategories$.value,
      skip: this.embroideries$.value.length,
      keepAlreadyLoadedEmbroideries: true,
    });
  }

  updateSearchTerm(value: string): void {
    this.searchValue$.next(value.trim());
  }

  updateSelectedCategories(items: DropdownItemId[]): void {
    this.selectedCategories$.next(new Set(items));
  }

  clearSelectedCategories(dropdown: DropdownComponent): void {
    dropdown.clearSelection();
    dropdown.toggleOpened();
  }

  private reloadEmbroideries(props: {
    searchTerm?: string;
    categories?: Set<string>;
    skip?: number;
    keepAlreadyLoadedEmbroideries?: boolean;
  }): void {
    const searchTerm = someOrNone(props.searchTerm).orElse('');
    const categories = someOrNone(props.categories).orElse(new Set<string>());
    const skip = someOrNone(props.skip).orElse(0);
    const keepAlreadyLoadedEmbroideries = someOrNone(props.keepAlreadyLoadedEmbroideries).orElse(false);

    this.loadingEmbroideries$.next(true);

    this.embroideriesService
      .getEmbroideries({ searchTerm, categories: Array.from(categories), skip, limit: EMBROIDERIES_LIMIT })
      .pipe(
        first(),
        finalize(() => {
          this.loadingEmbroideries$.next(false);
          this.embroideriesLoaded$.next(true);
        }),
      )
      .subscribe({
        next: (page) => {
          this.totalEmbroideries$.next(page.total);

          if (keepAlreadyLoadedEmbroideries) {
            this.embroideries$.next([...this.embroideries$.value, ...page.embroideries]);
          } else {
            this.embroideries$.next(page.embroideries);
          }
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            message: 'Die Stickereien konnten nicht geladen werden. Bitte versuche es erneut.',
            type: 'error',
          });
        },
      });
  }

  private reloadUsedCategories(): void {
    this.embroideryCategoriesService
      .getUsedCategories()
      .pipe(first())
      .subscribe({
        next: (categories) => this.usedCategories$.next(categories),
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            message: 'Die Kategorien konnten nicht geladen werden. Bitte versuche die Seite neu zu laden.',
            type: 'error',
          });
        },
      });
  }
}
