import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, catchError, combineLatest, delay, finalize, first, map, Observable } from 'rxjs';
import { environment } from '../../../../../../../environments';
import { PatternAttribution, PatternCategory, PatternExtra, PatternVariant } from '../../model';
import { ButtonSize, Chip, NotificationService } from '../../../../../shared';
import { PatternCategoriesService, PatternsService } from '../../services';
import { ActivatedRoute, Router } from '@angular/router';
import { ContentChange } from 'ngx-quill';
import { Delta } from 'quill/core';
import { someOrNone } from '@kicherkrabbe/shared';

@Component({
  selector: 'app-create-pattern-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class CreatePage implements OnInit, OnDestroy {
  private readonly name$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  private readonly nameTouched$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly nameValid$: Observable<boolean> = this.name$.pipe(map((name) => name.length > 0));
  protected readonly nameError$: Observable<boolean> = combineLatest([this.nameTouched$, this.nameValid$]).pipe(
    map(([touched, valid]) => touched && !valid),
  );

  protected readonly number$: BehaviorSubject<string> = new BehaviorSubject<string>('S-');
  private readonly numberTouched$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly numberValid$: Observable<boolean> = this.number$.pipe(
    map((number) => number.trim().length > 0 && number.startsWith('S-')),
  );
  protected readonly numberError$: Observable<boolean> = this.numberValid$.pipe(map((valid) => !valid));

  protected readonly imageIds$: BehaviorSubject<string[]> = new BehaviorSubject<string[]>([]);
  protected readonly imageUploadActive$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
  protected readonly imagesValid$: Observable<boolean> = this.imageIds$.pipe(map((imageIds) => imageIds.length > 0));
  protected readonly imagesError$: Observable<boolean> = this.imagesValid$.pipe(map((valid) => !valid));

  private readonly originalPatternName$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  private readonly designer$: BehaviorSubject<string> = new BehaviorSubject<string>('');

  private readonly description$: BehaviorSubject<Delta> = new BehaviorSubject<Delta>(new Delta());

  protected readonly availableCategories$: BehaviorSubject<PatternCategory[]> = new BehaviorSubject<PatternCategory[]>(
    [],
  );
  protected readonly loadingAvailableCategories$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
  protected readonly selectedCategories$: BehaviorSubject<PatternCategory[]> = new BehaviorSubject<PatternCategory[]>(
    [],
  );

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

  protected readonly watermark$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);

  protected readonly creating$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  protected readonly cannotSubmit$: Observable<boolean> = combineLatest([
    this.nameValid$,
    this.numberValid$,
    this.variantsValid$,
    this.extrasValid$,
    this.imagesValid$,
  ]).pipe(map(([name, number, variants, extras, images]) => !name || !number || !variants || !extras || !images));

  protected readonly ButtonSize = ButtonSize;
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
    private readonly notificationService: NotificationService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.reloadAvailableCategories();
  }

  ngOnDestroy(): void {
    this.name$.complete();
    this.nameTouched$.complete();
    this.number$.complete();
    this.numberTouched$.complete();
    this.creating$.complete();
    this.originalPatternName$.complete();
    this.designer$.complete();
    this.description$.complete();
    this.imageIds$.complete();
    this.imageUploadActive$.complete();
    this.availableCategories$.complete();
    this.loadingAvailableCategories$.complete();
    this.selectedCategories$.complete();
    this.extras$.complete();
    this.variants$.complete();
    this.watermark$.complete();
  }

  create(): void {
    this.creating$.next(true);

    const name = this.name$.value;
    const number = this.number$.value;
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
    const categories = this.selectedCategories$.value.map((c) => c.id);
    const images = this.imageIds$.value;
    const variants = this.variants$.value;
    const extras = this.extras$.value;
    const description = this.description$.value.ops.length > 0 ? JSON.stringify(this.description$.value) : null;

    this.patternsService
      .createPattern({
        name,
        number,
        description,
        attribution,
        categories,
        images,
        variants,
        extras,
      })
      .pipe(
        first(),
        catchError((e) => {
          console.error(e);
          this.notificationService.publish({
            type: 'error',
            message: 'Das Schnittmuster konnte nicht erstellt werden. Bitte versuche es erneut.',
          });
          return [];
        }),
        finalize(() => this.creating$.next(false)),
        delay(500),
      )
      .subscribe((patternId) => {
        this.notificationService.publish({
          type: 'success',
          message: 'Das Schnittmuster wurde erfolgreich erstellt.',
        });
        this.router.navigate(['..', patternId], {
          relativeTo: this.route,
        });
      });
  }

  updateName(value: string): void {
    this.name$.next(value.trim());

    if (!this.nameTouched$.value) {
      this.nameTouched$.next(true);
    }
  }

  updateNumber(value: string): void {
    this.number$.next(value.trim());

    if (!this.numberTouched$.value) {
      this.numberTouched$.next(true);
    }
  }

  updateOriginalPatternName(value: string): void {
    this.originalPatternName$.next(value.trim());
  }

  updateDesigner(value: string): void {
    this.designer$.next(value.trim());
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

  onImagesUploaded(imageIds: string[]): void {
    this.imageUploadActive$.next(false);
    this.imageIds$.next([...this.imageIds$.value, ...imageIds]);
  }

  activateImageUpload(): void {
    this.imageUploadActive$.next(true);
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  categoriesToChips(categories: PatternCategory[]): Chip[] {
    return categories.map((category) => this.categoryToChip(category));
  }

  onCategoryRemoved(chip: Chip): void {
    const categories = this.selectedCategories$.value.filter((category) => category.id !== chip.id);
    this.selectedCategories$.next(categories);
  }

  onCategoryAdded(chip: Chip): void {
    const category = this.availableCategories$.value.find((c) => c.id === chip.id);
    if (category) {
      const categories = [...this.selectedCategories$.value, category];
      this.selectedCategories$.next(categories);
    }
  }

  onExtrasChanged(extras: PatternExtra[]): void {
    this.extras$.next(extras);
  }

  onVariantsChanged(variants: PatternVariant[]): void {
    this.variants$.next(variants);
  }

  onWatermarkChanged(value: boolean): void {
    this.watermark$.next(value);
  }

  deleteImage(imageId: string): void {
    const imageIds = this.imageIds$.value.filter((id) => id !== imageId);
    this.imageIds$.next(imageIds);
  }

  private categoryToChip(category: PatternCategory): Chip {
    return Chip.of({
      id: category.id,
      label: category.name,
    });
  }

  private reloadAvailableCategories(): void {
    this.loadingAvailableCategories$.next(true);

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
        finalize(() => this.loadingAvailableCategories$.next(false)),
      )
      .subscribe((categories) => this.availableCategories$.next(categories));
  }
}
