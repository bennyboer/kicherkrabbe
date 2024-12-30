import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PatternCategoriesService, PatternsService } from '../../services';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  debounceTime,
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
import {
  ImageId,
  Pattern,
  PatternAttribution,
  PatternCategory,
  PatternCategoryId,
  PatternExtra,
  PatternId,
  PatternVariant,
} from '../../model';
import { Chip, NotificationService } from '../../../../../shared';
import { Eq } from '../../../../../../util';
import { environment } from '../../../../../../../environments';
import Quill, { Delta } from 'quill/core';
import { ContentChange } from 'ngx-quill';
import { none, Option, someOrNone } from '../../../../../shared/modules/option';

@Component({
  selector: 'app-pattern-page',
  templateUrl: './pattern.page.html',
  styleUrls: ['./pattern.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternPage implements OnInit, OnDestroy {
  protected readonly patternId$: BehaviorSubject<Option<PatternId>> = new BehaviorSubject<Option<PatternId>>(none());
  protected readonly pattern$: Subject<Pattern> = new ReplaySubject<Pattern>(1);
  protected readonly patternLoading$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);

  protected readonly categories$: BehaviorSubject<PatternCategory[]> = new BehaviorSubject<PatternCategory[]>([]);
  protected readonly categoriesLoading$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
  protected readonly selectedCategories$: BehaviorSubject<PatternCategoryId[]> = new BehaviorSubject<
    PatternCategoryId[]
  >([]);

  protected readonly name$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  private readonly nameValid$: Observable<boolean> = this.name$.pipe(map((name) => name.length > 0));
  protected readonly nameError$: Observable<boolean> = this.nameValid$.pipe(map((valid) => !valid));
  private readonly nameChanged$: Observable<boolean> = combineLatest([this.pattern$, this.name$]).pipe(
    map(([pattern, name]) => pattern.name !== name),
  );
  protected readonly cannotSaveUpdatedName$: Observable<boolean> = combineLatest([
    this.nameValid$,
    this.nameChanged$,
  ]).pipe(map(([valid, changed]) => !valid || !changed));

  protected readonly number$: BehaviorSubject<string> = new BehaviorSubject<string>('S-');
  protected readonly numberValid$: Observable<boolean> = this.number$.pipe(
    map((number) => number.length > 0 && number.startsWith('S-')),
  );
  protected readonly numberError$: Observable<boolean> = this.numberValid$.pipe(map((valid) => !valid));
  private readonly numberChanged$: Observable<boolean> = combineLatest([this.pattern$, this.number$]).pipe(
    map(([pattern, number]) => pattern.number !== number),
  );
  protected readonly cannotSaveUpdatedNumber$: Observable<boolean> = combineLatest([
    this.numberValid$,
    this.numberChanged$,
  ]).pipe(map(([valid, changed]) => !valid || !changed));

  protected readonly originalPatternName$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  protected readonly designer$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  protected readonly cannotSaveUpdatedAttribution$: Observable<boolean> = combineLatest([
    this.pattern$,
    this.originalPatternName$,
    this.designer$,
  ]).pipe(
    map(([pattern, originalPatternName, designer]) => {
      return (
        pattern.attribution.originalPatternName.orElse('') === originalPatternName &&
        pattern.attribution.designer.orElse('') === designer
      );
    }),
  );

  private readonly editor$: Subject<Quill> = new ReplaySubject(1);
  protected readonly description$: BehaviorSubject<Delta> = new BehaviorSubject<Delta>(new Delta());
  protected readonly cannotSaveUpdatedDescription$: Observable<boolean> = combineLatest([
    this.pattern$,
    this.description$.pipe(map((d) => (d.ops.length > 0 ? JSON.stringify(d) : null))),
  ]).pipe(map(([pattern, description]) => pattern.description.orElse('') === someOrNone(description).orElse('')));

  protected readonly variants$: BehaviorSubject<PatternVariant[]> = new BehaviorSubject<PatternVariant[]>([]);
  protected readonly variantsValid$: Observable<boolean> = this.variants$.pipe(
    map((variants) => variants.length > 0 && variants.every((v) => v.name.trim().length > 0 && v.sizes.length > 0)),
  );
  protected readonly variantsError$: Observable<boolean> = this.variantsValid$.pipe(map((valid) => !valid));

  protected readonly extras$: BehaviorSubject<PatternExtra[]> = new BehaviorSubject<PatternExtra[]>([]);
  protected readonly extrasValid$: Observable<boolean> = this.extras$.pipe(
    map((extras) => extras.every((e) => e.name.trim().length > 0)),
  );
  protected readonly extrasError$: Observable<boolean> = this.extrasValid$.pipe(map((valid) => !valid));

  protected readonly imageIds$: BehaviorSubject<string[]> = new BehaviorSubject<string[]>([]);
  protected readonly imageUploadActive$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly imagesValid$: Observable<boolean> = this.imageIds$.pipe(map((imageIds) => imageIds.length > 0));
  protected readonly imagesError$: Observable<boolean> = this.imagesValid$.pipe(map((valid) => !valid));
  protected readonly imagesSortableConfig: any = {
    delay: 300,
    delayOnTouchOnly: true,
    touchStartThreshold: 10,
    onUpdate: () => {
      this.imageIds$.next(this.imageIds$.value);
    },
  };

  protected readonly deleteConfirmationRequired$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  protected readonly watermark$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);

  protected readonly loading$: Observable<boolean> = combineLatest([
    this.patternLoading$,
    this.categoriesLoading$,
  ]).pipe(map(([patternLoading, categoriesLoading]) => patternLoading || categoriesLoading));
  protected readonly notLoading$: Observable<boolean> = this.loading$.pipe(map((loading) => !loading));

  private readonly destroy$: Subject<void> = new Subject<void>();

  protected readonly quillModules = {
    toolbar: [
      [{ header: [1, 2, false] }],
      ['bold', 'italic', 'underline', 'strike', 'link'],
      [{ align: [] }],
      [{ color: [] }, { background: [] }],
      ['blockquote', { list: 'ordered' }, { list: 'bullet' }],
      ['clean'],
    ],
  };

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

    combineLatest([this.pattern$, this.selectedCategories$.pipe(debounceTime(300))])
      .pipe(
        filter(
          ([pattern, categories]) => !this.areSetsEqual(pattern.categories, new Set<PatternCategoryId>(categories)),
        ),
        takeUntil(this.destroy$),
      )
      .subscribe(([pattern, categories]) => this.saveUpdatedCategories(pattern, categories));

    combineLatest([this.pattern$, this.variants$.pipe(debounceTime(300)), this.variantsValid$])
      .pipe(
        filter(([pattern, variants, valid]) => valid && !this.areArraysEqual(pattern.variants, variants)),
        takeUntil(this.destroy$),
      )
      .subscribe(([pattern, variants, _valid]) => this.saveUpdatedVariants(pattern, variants));

    combineLatest([this.pattern$, this.extras$.pipe(debounceTime(300)), this.extrasValid$])
      .pipe(
        filter(([pattern, extras, valid]) => valid && !this.areArraysEqual(pattern.extras, extras)),
        takeUntil(this.destroy$),
      )
      .subscribe(([pattern, extras, _valid]) => this.saveUpdatedExtras(pattern, extras));

    combineLatest([this.pattern$, this.imageIds$.pipe(debounceTime(300)), this.imagesValid$])
      .pipe(
        debounceTime(200),
        filter(([pattern, imageIds, valid]) => valid && !this.arePrimitiveArraysEqual(pattern.images, imageIds)),
        takeUntil(this.destroy$),
      )
      .subscribe(([pattern, imageIds, _valid]) => this.saveUpdatedImages(pattern, imageIds));

    combineLatest([this.editor$, this.description$])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([editor, description]) => {
        const currentContents = editor.getContents();
        const expectedContents = description;

        if (expectedContents.ops.length === 0) {
          return;
        }

        if (JSON.stringify(currentContents) !== JSON.stringify(expectedContents)) {
          editor.setContents(description);
        }
      });
  }

  ngOnDestroy(): void {
    this.pattern$.complete();
    this.patternLoading$.complete();
    this.categories$.complete();
    this.categoriesLoading$.complete();
    this.selectedCategories$.complete();
    this.deleteConfirmationRequired$.complete();
    this.name$.complete();
    this.editor$.complete();
    this.description$.complete();
    this.originalPatternName$.complete();
    this.designer$.complete();
    this.variants$.complete();
    this.extras$.complete();
    this.imageIds$.complete();
    this.imageUploadActive$.complete();
    this.watermark$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  onWatermarkChanged(value: boolean): void {
    this.watermark$.next(value);
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

  updateNumber(value: string): void {
    this.number$.next(value);
  }

  saveUpdatedNumber(pattern: Pattern): void {
    const changes$ = this.patternsService.getPatternChanges();

    const number = this.number$.value;
    this.patternsService
      .updatePatternNumber(pattern.id, pattern.version, number)
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
            message: `Die Nummber des Schnittmusters wurde zu „${number}“ geändert`,
          });
          this.reloadPattern({ id: pattern.id, indicateLoading: false });
        },
        error: (e) => {
          const reason = e?.error?.reason;
          if (reason === 'NUMBER_ALREADY_IN_USE') {
            this.notificationService.publish({
              type: 'error',
              message: 'Die Nummer des Schnittmusters ist bereits vergeben',
            });
          } else {
            console.error(e);
            this.notificationService.publish({
              type: 'error',
              message: 'Die Nummer des Schnittmusters konnte nicht geändert werden',
            });
          }
        },
      });
  }

  saveUpdatedDescription(pattern: Pattern): void {
    const changes$ = this.patternsService.getPatternChanges();

    const description = this.description$.value.ops.length > 0 ? JSON.stringify(this.description$.value) : null;
    this.patternsService
      .updateDescription(pattern.id, pattern.version, description)
      .pipe(
        switchMap(() => {
          return changes$.pipe(
            filter((patterns) => patterns.has(pattern.id)),
            first(),
            map(() => null),
            timeout(5000),
            catchError((e) => {
              console.warn('Pattern description update not confirmed', e);
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
            message: `Die Beschreibung des Schnittmusters wurde geändert`,
          });
          this.reloadPattern({ id: pattern.id, indicateLoading: false });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Beschreibung des Schnittmusters konnte nicht geändert werden',
          });
        },
      });
  }

  updateOriginalPatternName(value: string): void {
    this.originalPatternName$.next(value.trim());
  }

  updateDesigner(value: string): void {
    this.designer$.next(value.trim());
  }

  onEditorCreated(quill: Quill): void {
    this.editor$.next(quill);
  }

  updateDescription(event: ContentChange): void {
    const html = someOrNone(event.html)
      .map((h) => h.trim())
      .orElse('');
    const isEmpty = html.length === 0;
    if (isEmpty) {
      this.description$.next(new Delta());
    } else {
      this.description$.next(event.content);
    }
  }

  publishPattern(pattern: Pattern): void {
    const changes$ = this.patternsService.getPatternChanges();

    this.patternsService
      .publishPattern(pattern.id, pattern.version)
      .pipe(
        switchMap(() => {
          return changes$.pipe(
            filter((patterns) => patterns.has(pattern.id)),
            first(),
            map(() => null),
            timeout(5000),
            catchError((e) => {
              console.warn('Pattern publish not confirmed', e);
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
            message: 'Das Schnittmuster wurde veröffentlicht',
          });
          this.reloadPattern({ id: pattern.id, indicateLoading: false });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Das Schnittmuster konnte nicht veröffentlicht werden',
          });
        },
      });
  }

  unpublishPattern(pattern: Pattern): void {
    const changes$ = this.patternsService.getPatternChanges();

    this.patternsService
      .unpublishPattern(pattern.id, pattern.version)
      .pipe(
        switchMap(() => {
          return changes$.pipe(
            filter((patterns) => patterns.has(pattern.id)),
            first(),
            map(() => null),
            timeout(5000),
            catchError((e) => {
              console.warn('Pattern unpublish not confirmed', e);
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
            message: 'Die Veröffentlichung des Schnittmusters wurde aufgehoben',
          });
          this.reloadPattern({ id: pattern.id, indicateLoading: false });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Veröffentlichung des Schnittmusters konnte nicht aufgehoben werden',
          });
        },
      });
  }

  saveUpdatedAttribution(pattern: Pattern): void {
    const changes$ = this.patternsService.getPatternChanges();

    const originalPatternName = someOrNone(this.originalPatternName$.value)
      .map((n) => n.trim())
      .filter((n) => n.length > 0)
      .orElseNull();
    const designer = someOrNone(this.designer$.value)
      .map((n) => n.trim())
      .filter((n) => n.length > 0)
      .orElseNull();
    const attribution = PatternAttribution.of({
      originalPatternName,
      designer,
    });

    this.patternsService
      .updateAttribution(pattern.id, pattern.version, attribution)
      .pipe(
        switchMap(() => {
          return changes$.pipe(
            filter((patterns) => patterns.has(pattern.id)),
            first(),
            map(() => null),
            timeout(5000),
            catchError((e) => {
              console.warn('Pattern attribution update not confirmed', e);
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
            message: `Der Originalname und der Designer des Schnittmusters wurden geändert`,
          });
          this.reloadPattern({ id: pattern.id, indicateLoading: false });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Der Originalname und der Designer des Schnittmusters konnten nicht geändert werden',
          });
        },
      });
  }

  saveUpdatedCategories(pattern: Pattern, categories: PatternCategoryId[]): void {
    const changes$ = this.patternsService.getPatternChanges();

    this.patternsService
      .updateCategories(pattern.id, pattern.version, categories)
      .pipe(
        switchMap(() => {
          return changes$.pipe(
            filter((patterns) => patterns.has(pattern.id)),
            first(),
            map(() => null),
            timeout(5000),
            catchError((e) => {
              console.warn('Pattern category update not confirmed', e);
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
            message: `Die Kategorien des Schnittmusters wurden geändert`,
          });
          this.reloadPattern({ id: pattern.id, indicateLoading: false });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Kategorien des Schnittmusters konnten nicht geändert werden',
          });
        },
      });
  }

  saveUpdatedVariants(pattern: Pattern, variants: PatternVariant[]): void {
    const changes$ = this.patternsService.getPatternChanges();

    this.patternsService
      .updateVariants(pattern.id, pattern.version, variants)
      .pipe(
        switchMap(() => {
          return changes$.pipe(
            filter((patterns) => patterns.has(pattern.id)),
            first(),
            map(() => null),
            timeout(5000),
            catchError((e) => {
              console.warn('Pattern variants update not confirmed', e);
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
            message: `Die Varianten des Schnittmusters wurden geändert`,
          });
          this.reloadPattern({ id: pattern.id, indicateLoading: false });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Varianten des Schnittmusters konnten nicht geändert werden',
          });
        },
      });
  }

  saveUpdatedExtras(pattern: Pattern, extras: PatternExtra[]): void {
    const changes$ = this.patternsService.getPatternChanges();

    this.patternsService
      .updateExtras(pattern.id, pattern.version, extras)
      .pipe(
        switchMap(() => {
          return changes$.pipe(
            filter((patterns) => patterns.has(pattern.id)),
            first(),
            map(() => null),
            timeout(5000),
            catchError((e) => {
              console.warn('Pattern extras update not confirmed', e);
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
            message: `Die Extras des Schnittmusters wurden geändert`,
          });
          this.reloadPattern({ id: pattern.id, indicateLoading: false });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Extras des Schnittmusters konnten nicht geändert werden',
          });
        },
      });
  }

  saveUpdatedImages(pattern: Pattern, images: ImageId[]): void {
    const changes$ = this.patternsService.getPatternChanges();

    this.patternsService
      .updateImages(pattern.id, pattern.version, images)
      .pipe(
        switchMap(() => {
          return changes$.pipe(
            filter((patterns) => patterns.has(pattern.id)),
            first(),
            map(() => null),
            timeout(5000),
            catchError((e) => {
              console.warn('Pattern images update not confirmed', e);
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
            message: `Die Bilder des Schnittmusters wurden geändert`,
          });
          this.reloadPattern({ id: pattern.id, indicateLoading: false });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Bilder des Schnittmusters konnten nicht geändert werden',
          });
        },
      });
  }

  delete(pattern: Pattern): void {
    const changes$ = this.patternsService.getPatternChanges();

    this.patternsService
      .deletePattern(pattern.id, pattern.version)
      .pipe(
        switchMap(() => {
          return changes$.pipe(
            filter((patterns) => patterns.has(pattern.id)),
            first(),
            map(() => null),
            timeout(5000),
            catchError((e) => {
              console.warn('Pattern deletion not confirmed', e);
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

  onVariantsChanged(variants: PatternVariant[]): void {
    this.variants$.next(variants);
  }

  onExtrasChanged(extras: PatternExtra[]): void {
    this.extras$.next(extras);
  }

  onImagesUploaded(imageIds: string[]): void {
    this.imageUploadActive$.next(false);
    this.imageIds$.next([...this.imageIds$.value, ...imageIds]);
  }

  activateImageUpload(): void {
    this.imageUploadActive$.next(true);
  }

  deleteImage(imageId: string): void {
    const imageIds = this.imageIds$.value.filter((id) => id !== imageId);
    this.imageIds$.next(imageIds);
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  toCategories(ids: PatternCategoryId[], categories: PatternCategory[]): PatternCategory[] {
    const lookup: Map<PatternCategoryId, PatternCategory> = new Map<PatternCategoryId, PatternCategory>();
    for (const category of categories) {
      lookup.set(category.id, category);
    }

    const result = ids.map((id) => lookup.get(id)).filter((category) => !!category);

    result.sort((a, b) =>
      a.name.localeCompare(b.name, 'de-de', {
        numeric: true,
      }),
    );

    return result;
  }

  categoriesToChips(categories: PatternCategory[]): Chip[] {
    return categories.map((category) => this.categoryToChip(category));
  }

  onCategoryRemoved(chip: Chip): void {
    const categories = this.selectedCategories$.value.filter((category) => category !== chip.id);
    this.selectedCategories$.next(categories);
  }

  onCategoryAdded(chip: Chip): void {
    const category = this.categories$.value.find((c) => c.id === chip.id);
    if (category) {
      const categories = [...this.selectedCategories$.value, category.id];
      this.selectedCategories$.next(categories);
    }
  }

  private categoryToChip(category: PatternCategory): Chip {
    return Chip.of({
      id: category.id,
      label: category.name,
    });
  }

  private reloadPattern(props: { id: PatternId; indicateLoading?: boolean }): void {
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
          this.number$.next(pattern.number);
          pattern.description.ifSome((description) => this.description$.next(new Delta(JSON.parse(description))));
          this.originalPatternName$.next(pattern.attribution.originalPatternName.orElse(''));
          this.designer$.next(pattern.attribution.designer.orElse(''));
          this.selectedCategories$.next(Array.from(pattern.categories));

          if (!this.areArraysEqual(pattern.variants, this.variants$.value)) {
            this.variants$.next(pattern.variants);
          }

          if (!this.areArraysEqual(pattern.extras, this.extras$.value)) {
            this.extras$.next(pattern.extras);
          }

          if (!this.arePrimitiveArraysEqual(pattern.images, this.imageIds$.value)) {
            this.imageIds$.next([...pattern.images]);
          }
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
            message: 'Die Kategorien konnten nicht geladen werden. Bitte versuche die Seite neuzuladen.',
          });
          return [];
        }),
        finalize(() => this.categoriesLoading$.next(false)),
      )
      .subscribe((categories) => this.categories$.next(categories));
  }

  private areSetsEqual<T>(a: Set<T>, b: Set<T>): boolean {
    if (a.size !== b.size) {
      return false;
    }

    for (const item of a) {
      if (!b.has(item)) {
        return false;
      }
    }

    return true;
  }

  private areArraysEqual<T extends Eq<T>>(a: T[], b: T[]): boolean {
    if (a.length !== b.length) {
      return false;
    }

    for (let i = 0; i < a.length; i++) {
      if (!a[i].equals(b[i])) {
        return false;
      }
    }

    return true;
  }

  private arePrimitiveArraysEqual<T>(a: T[], b: T[]): boolean {
    if (a.length !== b.length) {
      return false;
    }

    for (let i = 0; i < a.length; i++) {
      if (a[i] !== b[i]) {
        return false;
      }
    }

    return true;
  }
}
