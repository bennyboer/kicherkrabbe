import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  filter,
  finalize,
  first,
  map,
  Observable,
  of,
  ReplaySubject,
  Subject,
  switchMap,
  takeUntil,
  timeout,
} from 'rxjs';
import { CategoriesService } from '../../services';
import { ActivatedRoute, Router } from '@angular/router';
import { Category, CategoryGroup, CategoryGroupType, CategoryId, GROUPS } from '../../model';
import { DropdownComponent, DropdownItem, DropdownItemId, NotificationService } from '../../../../../shared';
import { none, Option, someOrNone } from '../../../../../shared/modules/option';

@Component({
  selector: 'app-category-page',
  templateUrl: './category.page.html',
  styleUrls: ['./category.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class CategoryPage implements OnInit, OnDestroy {
  protected readonly category$: Subject<Category> = new ReplaySubject(1);
  protected readonly loading$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
  protected readonly notLoading$: Observable<boolean> = this.loading$.pipe(map((loading) => !loading));
  protected readonly deleteConfirmationRequired$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly categoryId$: BehaviorSubject<Option<CategoryId>> = new BehaviorSubject<Option<CategoryId>>(none());
  private readonly destroy$: Subject<void> = new Subject<void>();

  private readonly name$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  private readonly nameChanged$: Observable<boolean> = combineLatest([this.category$, this.name$]).pipe(
    map(([category, name]) => category.name !== name),
  );
  private readonly nameValid$: Observable<boolean> = this.name$.pipe(map((name) => name.length > 0));
  protected readonly nameError$: Observable<boolean> = this.nameValid$.pipe(map((valid) => !valid));
  protected readonly cannotSaveUpdatedName$: Observable<boolean> = combineLatest([
    this.nameValid$,
    this.nameChanged$,
  ]).pipe(map(([valid, changed]) => !valid || !changed));

  private readonly groups$: BehaviorSubject<CategoryGroup[]> = new BehaviorSubject<CategoryGroup[]>(GROUPS);
  private readonly selectedGroup$: Observable<CategoryGroup> = this.category$.pipe(map((category) => category.group));
  protected readonly groupDropdownItems$: Observable<DropdownItem[]> = this.groups$.pipe(
    map((groups) => groups.map((g) => ({ id: g.type, label: g.name }))),
  );
  protected readonly initialSelectedDropdownItems$: Observable<DropdownItemId[]> = this.selectedGroup$.pipe(
    map((group) => [group.type]),
    first(),
  );

  constructor(
    private readonly categoriesService: CategoriesService,
    private readonly notificationService: NotificationService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.route.params
      .pipe(
        map((params) => params['id']),
        takeUntil(this.destroy$),
      )
      .subscribe((id) => this.categoryId$.next(someOrNone(id)));

    this.categoryId$
      .pipe(
        filter((id) => id.isSome()),
        map((id) => id.orElseThrow()),
        takeUntil(this.destroy$),
      )
      .subscribe((id) => this.reloadCategory({ id }));
  }

  ngOnDestroy(): void {
    this.category$.complete();
    this.categoryId$.complete();
    this.loading$.complete();
    this.deleteConfirmationRequired$.complete();
    this.name$.complete();
    this.groups$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateName(value: string): void {
    this.name$.next(value);
  }

  saveUpdatedName(category: Category): void {
    const name = this.name$.value;
    this.categoriesService
      .renameCategory(category.id, category.version, name)
      .pipe(
        switchMap(() => {
          return this.categoriesService.getCategoryChanges().pipe(
            filter((categories) => categories.has(category.id)),
            first(),
            map(() => null),
            timeout(5000),
            catchError((e) => {
              console.warn('Category update not confirmed', e);
              return of(null);
            }),
          );
        }),
        takeUntil(this.destroy$),
      )
      .subscribe({
        next: () => {
          this.notificationService.publish({
            type: 'success',
            message: `Der Name der Kategorie wurde zu „${name}“ geändert`,
          });
          this.reloadCategory({ id: category.id, indicateLoading: false });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Der Name der Kategorie konnte nicht geändert werden',
          });
        },
      });
  }

  updateSelectedGroup(category: Category, dropdown: DropdownComponent, items: DropdownItemId[]): void {
    if (items.length !== 1) {
      throw new Error('Only one group can be selected');
    }

    dropdown.toggleOpened();

    const item = items[0];
    const groupType: CategoryGroupType = this.dropdownItemToCategoryGroupType(item);
    someOrNone(this.groups$.value.find((g) => g.type === groupType)).ifSome((group) =>
      this.categoriesService
        .regroupCategory(category.id, category.version, group)
        .pipe(
          switchMap(() => {
            return this.categoriesService.getCategoryChanges().pipe(
              filter((categories) => categories.has(category.id)),
              first(),
              map(() => null),
              timeout(5000),
              catchError((e) => {
                console.warn('Category regrouping not confirmed', e);
                return of(null);
              }),
            );
          }),
        )
        .subscribe({
          next: () => {
            this.notificationService.publish({
              type: 'success',
              message: `Die Gruppe der Kategorie wurde zu ${group.name} geändert`,
            });
            this.reloadCategory({ id: category.id, indicateLoading: false });
          },
          error: (e) => {
            console.error(e);
            this.notificationService.publish({
              type: 'error',
              message: 'Die Kategorie konnte nicht umgruppiert werden',
            });
          },
        }),
    );
  }

  delete(category: Category): void {
    this.categoriesService
      .deleteCategory(category.id, category.version)
      .pipe(
        switchMap(() => {
          return this.categoriesService.getCategoryChanges().pipe(
            filter((categories) => categories.has(category.id)),
            first(),
            map(() => null),
            timeout(5000),
            catchError((e) => {
              console.warn('Category deletion not confirmed', e);
              return of(null);
            }),
          );
        }),
        takeUntil(this.destroy$),
      )
      .subscribe({
        next: () => {
          this.notificationService.publish({
            type: 'success',
            message: 'Die Kategorie wurde gelöscht',
          });
          this.router.navigate(['..'], { relativeTo: this.route });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Kategorie konnte nicht gelöscht werden',
          });
        },
      });
  }

  waitForDeleteConfirmation(): void {
    this.deleteConfirmationRequired$.next(true);
  }

  private reloadCategory(props: { id: CategoryId; indicateLoading?: boolean }): void {
    const id = props.id;
    const indicateLoading = someOrNone(props.indicateLoading).orElse(true);
    if (indicateLoading) {
      this.loading$.next(true);
    }

    this.categoriesService
      .getCategory(id)
      .pipe(
        first(),
        finalize(() => this.loading$.next(false)),
        takeUntil(this.destroy$),
      )
      .subscribe({
        next: (category) => {
          this.category$.next(category);
          this.name$.next(category.name);
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Kategorie konnte nicht geladen werden',
          });
        },
      });
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
}
