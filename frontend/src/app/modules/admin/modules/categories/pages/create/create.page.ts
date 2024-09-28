import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { CategoriesService } from '../../services';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  filter,
  first,
  map,
  Observable,
  of,
  Subject,
  switchMap,
  takeUntil,
  timeout,
} from 'rxjs';
import { CategoryGroup, CategoryGroupType, GROUPS, NONE } from '../../model';
import {
  DropdownComponent,
  DropdownItem,
  DropdownItemId,
  NotificationService,
} from '../../../../../shared';
import { ActivatedRoute, Router } from '@angular/router';
import { someOrNone } from '../../../../../shared/modules/option';

@Component({
  selector: 'app-create-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreatePage implements OnDestroy {
  private readonly name$: BehaviorSubject<string> = new BehaviorSubject<string>(
    '',
  );
  private readonly nameTouched$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly nameValid$: Observable<boolean> = this.name$.pipe(
    map((name) => name.length > 0),
  );
  protected readonly nameError$: Observable<boolean> = combineLatest([
    this.nameTouched$,
    this.nameValid$,
  ]).pipe(map(([touched, valid]) => touched && !valid));

  private readonly groups$: BehaviorSubject<CategoryGroup[]> =
    new BehaviorSubject<CategoryGroup[]>(GROUPS);
  private readonly selectedGroup$: BehaviorSubject<CategoryGroup> =
    new BehaviorSubject<CategoryGroup>(NONE);
  protected readonly groupDropdownItems$: Observable<DropdownItem[]> =
    this.groups$.pipe(
      map((groups) => groups.map((g) => ({ id: g.type, label: g.name }))),
    );
  protected readonly initialSelectedDropdownItems$: Observable<
    DropdownItemId[]
  > = this.selectedGroup$.pipe(
    map((group) => [group.type]),
    first(),
  );
  private readonly selectedGroupValid$: Observable<boolean> =
    this.selectedGroup$.pipe(map((group) => !!group));
  private readonly formValid$: Observable<boolean> = combineLatest([
    this.nameValid$,
    this.selectedGroupValid$,
  ]).pipe(map(([nameValid, groupValid]) => nameValid && groupValid));

  protected readonly creating$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  protected readonly cannotSubmit$: Observable<boolean> = combineLatest([
    this.formValid$,
    this.creating$,
  ]).pipe(map(([formValid, creating]) => !formValid || creating));

  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly categoriesService: CategoriesService,
    private readonly notificationService: NotificationService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
  ) {}

  ngOnDestroy(): void {
    this.name$.complete();
    this.nameTouched$.complete();
    this.groups$.complete();
    this.selectedGroup$.complete();
    this.creating$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateName(name: string): void {
    this.name$.next(name);

    if (!this.nameTouched$.value) {
      this.nameTouched$.next(true);
    }
  }

  updateSelectedGroup(
    dropdown: DropdownComponent,
    items: DropdownItemId[],
  ): void {
    if (items.length !== 1) {
      throw new Error('Only one group can be selected');
    }

    const item = items[0];
    const groupType: CategoryGroupType =
      this.dropdownItemToCategoryGroupType(item);
    someOrNone(this.groups$.value.find((g) => g.type === groupType)).ifSome(
      (group) => this.selectedGroup$.next(group),
    );

    dropdown.toggleOpened();
  }

  create(): void {
    const name = this.name$.value;
    const group = this.selectedGroup$.value;

    this.creating$.next(true);
    this.categoriesService
      .createCategory({
        name,
        group,
      })
      .pipe(
        switchMap((categoryId) => {
          return this.categoriesService.getCategoryChanges().pipe(
            filter((categories) => categories.has(categoryId)),
            first(),
            map(() => categoryId),
            timeout(5000),
            catchError((e) => {
              console.warn('Category creation not confirmed', e);
              return of(categoryId);
            }),
          );
        }),
        takeUntil(this.destroy$),
      )
      .subscribe({
        next: (categoryId) => {
          this.creating$.next(false);
          this.notificationService.publish({
            type: 'success',
            message: `Die Kategorie „${name}“ wurde erstellt.`,
          });
          this.router.navigate(['..', categoryId], {
            relativeTo: this.route,
          });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Kategorie konnte nicht erstellt werden.',
          });
        },
      });
  }

  private dropdownItemToCategoryGroupType(
    item: DropdownItemId,
  ): CategoryGroupType {
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
