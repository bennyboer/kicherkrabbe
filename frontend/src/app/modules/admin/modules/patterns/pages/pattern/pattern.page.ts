import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PatternCategoriesService, PatternsService } from '../../services';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  delay,
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
import { Pattern, PatternCategory, PatternId } from '../../model';
import { NotificationService } from '../../../../../shared';
import { none, Option, someOrNone } from '../../../../../../util';

@Component({
  selector: 'app-pattern-page',
  templateUrl: './pattern.page.html',
  styleUrls: ['./pattern.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternPage implements OnInit, OnDestroy {
  protected readonly patternId$: BehaviorSubject<Option<PatternId>> =
    new BehaviorSubject<Option<PatternId>>(none());
  protected readonly pattern$: Subject<Pattern> = new ReplaySubject<Pattern>(1);
  protected readonly patternLoading$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);

  protected readonly categories$: BehaviorSubject<PatternCategory[]> =
    new BehaviorSubject<PatternCategory[]>([]);
  protected readonly categoriesLoading$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);

  protected readonly name$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');
  private readonly nameValid$: Observable<boolean> = this.name$.pipe(
    map((name) => name.length > 0),
  );
  protected readonly nameError$: Observable<boolean> = this.nameValid$.pipe(
    map((valid) => !valid),
  );
  private readonly nameChanged$: Observable<boolean> = combineLatest([
    this.pattern$,
    this.name$,
  ]).pipe(map(([pattern, name]) => pattern.name !== name));
  protected readonly cannotSaveUpdatedName$: Observable<boolean> =
    combineLatest([this.nameValid$, this.nameChanged$]).pipe(
      map(([valid, changed]) => !valid || !changed),
    );

  protected readonly deleteConfirmationRequired$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);

  protected readonly loading$: Observable<boolean> = combineLatest([
    this.patternLoading$,
    this.categoriesLoading$,
  ]).pipe(
    map(
      ([patternLoading, categoriesLoading]) =>
        patternLoading || categoriesLoading,
    ),
  );
  protected readonly notLoading$: Observable<boolean> = this.loading$.pipe(
    map((loading) => !loading),
  );

  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly patternsService: PatternsService,
    private readonly patternCategoriesService: PatternCategoriesService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.reloadAvailableCategories();

    this.route.params
      .pipe(
        map((params) => params['id']),
        takeUntil(this.destroy$),
      )
      .subscribe((id) => this.patternId$.next(someOrNone(id)));

    this.patternId$
      .pipe(
        filter((id) => id.isSome()),
        map((id) => id.orElseThrow()),
        takeUntil(this.destroy$),
      )
      .subscribe((id) => this.reloadPattern({ id }));
  }

  ngOnDestroy(): void {
    this.pattern$.complete();
    this.patternLoading$.complete();
    this.categories$.complete();
    this.categoriesLoading$.complete();
    this.deleteConfirmationRequired$.complete();
    this.name$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateName(value: string): void {
    this.name$.next(value);
  }

  saveUpdatedName(pattern: Pattern): void {
    const changes$ = this.patternsService.getPatternChanges();

    const name = this.name$.value;
    this.patternsService
      .renamePattern(pattern.id, pattern.version, name)
      .pipe(
        switchMap(() => {
          return changes$.pipe(
            filter((patterns) => patterns.has(pattern.id)),
            first(),
            map(() => null),
            timeout(5000),
            catchError((e) => {
              console.warn('Pattern update not confirmed', e);
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
            message: `Der Name des Schnittmusters wurde zu „${name}“ geändert`,
          });
          this.reloadPattern({ id: pattern.id, indicateLoading: false });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Der Name des Schnittmusters konnte nicht geändert werden',
          });
        },
      });
  }

  delete(pattern: Pattern): void {
    this.patternsService
      .deletePattern(pattern.id, pattern.version)
      .pipe(delay(500), takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            type: 'success',
            message: 'Das Schnittmuster wurde gelöscht',
          });
          this.router.navigate(['..'], { relativeTo: this.route });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Das Schnittmuster konnte nicht gelöscht werden',
          });
        },
      });
  }

  waitForDeleteConfirmation(): void {
    this.deleteConfirmationRequired$.next(true);
  }

  private reloadPattern(props: {
    id: PatternId;
    indicateLoading?: boolean;
  }): void {
    const id = props.id;
    const indicateLoading = someOrNone(props.indicateLoading).orElse(true);
    if (indicateLoading) {
      this.patternLoading$.next(true);
    }

    this.patternsService
      .getPattern(id)
      .pipe(
        first(),
        finalize(() => this.patternLoading$.next(false)),
        takeUntil(this.destroy$),
      )
      .subscribe({
        next: (pattern) => {
          this.pattern$.next(pattern);
          this.name$.next(pattern.name);
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Das Schnittmuster konnte nicht geladen werden',
          });
        },
      });
  }

  private reloadAvailableCategories(): void {
    this.categoriesLoading$.next(true);

    this.patternCategoriesService
      .getAvailableCategories()
      .pipe(
        first(),
        catchError((e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message:
              'Die Kategorien konnten nicht geladen werden. Bitte versuche die Seite neuzuladen.',
          });
          return [];
        }),
        finalize(() => this.categoriesLoading$.next(false)),
      )
      .subscribe((categories) => this.categories$.next(categories));
  }
}
