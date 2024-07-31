import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  BehaviorSubject,
  combineLatest,
  delay,
  finalize,
  map,
  Observable,
  of,
} from 'rxjs';
import { environment } from '../../../../../../../environments';
import { PatternCategory, PatternExtra, PatternVariant } from '../../model';
import { ButtonSize, Chip } from '../../../../../shared';

@Component({
  selector: 'app-create-pattern-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreatePage implements OnInit, OnDestroy {
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

  protected readonly imageIds$: BehaviorSubject<string[]> = new BehaviorSubject<
    string[]
  >([]);
  protected readonly imageUploadActive$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);

  private readonly originalPatternName$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');
  private readonly attribution$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');

  protected readonly availableCategories$: BehaviorSubject<PatternCategory[]> =
    new BehaviorSubject<PatternCategory[]>([]);
  protected readonly loadingAvailableCategories$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);
  protected readonly selectedCategories$: BehaviorSubject<PatternCategory[]> =
    new BehaviorSubject<PatternCategory[]>([]);

  protected readonly variants$: BehaviorSubject<PatternVariant[]> =
    new BehaviorSubject<PatternVariant[]>([]);
  protected readonly extras$: BehaviorSubject<PatternExtra[]> =
    new BehaviorSubject<PatternExtra[]>([]);

  protected readonly creating$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);

  protected readonly cannotSubmit$: Observable<boolean> = this.nameValid$.pipe(
    map((valid) => !valid),
  );

  protected readonly ButtonSize = ButtonSize;

  ngOnInit(): void {
    this.reloadAvailableCategories();
  }

  ngOnDestroy(): void {
    this.name$.complete();
    this.nameTouched$.complete();
    this.creating$.complete();
    this.originalPatternName$.complete();
    this.attribution$.complete();
    this.imageIds$.complete();
    this.imageUploadActive$.complete();
    this.availableCategories$.complete();
    this.loadingAvailableCategories$.complete();
    this.selectedCategories$.complete();
    this.extras$.complete();
    this.variants$.complete();
  }

  create(): void {
    this.creating$.next(true);

    const name = this.name$.value;
    const originalPatternName = this.originalPatternName$.value;
    const attribution = this.attribution$.value;
    const imageIds = this.imageIds$.value;
    const variants = this.variants$.value;
    const extras = this.extras$.value;

    console.log('Create pattern', {
      name,
      originalPatternName,
      attribution,
      imageIds,
      variants,
      extras,
    }); // TODO
  }

  updateName(value: string): void {
    this.name$.next(value.trim());

    if (!this.nameTouched$.value) {
      this.nameTouched$.next(true);
    }
  }

  updateOriginalPatternName(value: string): void {
    this.originalPatternName$.next(value.trim());
  }

  updateAttribution(value: string): void {
    this.attribution$.next(value.trim());
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
    const categories = this.selectedCategories$.value.filter(
      (category) => category.id !== chip.id,
    );
    this.selectedCategories$.next(categories);
  }

  onCategoryAdded(chip: Chip): void {
    const category = this.availableCategories$.value.find(
      (c) => c.id === chip.id,
    );
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

  private categoryToChip(category: PatternCategory): Chip {
    return Chip.of({
      id: category.id,
      label: category.name,
    });
  }

  private reloadAvailableCategories(): void {
    this.loadingAvailableCategories$.next(true);

    // TODO Replace with backend call
    const mockCategories = [
      PatternCategory.of({ id: 'top', name: 'Oberteil' }),
      PatternCategory.of({ id: 'trousers', name: 'Hose' }),
      PatternCategory.of({ id: 'onesie', name: 'Einteiler' }),
      PatternCategory.of({ id: 'dress', name: 'Kleid' }),
      PatternCategory.of({ id: 'accessoire', name: 'Accessoire' }),
    ];

    of(mockCategories)
      .pipe(
        delay(500),
        finalize(() => this.loadingAvailableCategories$.next(false)),
      )
      .subscribe((categories) => this.availableCategories$.next(categories));
  }
}
