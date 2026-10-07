import { ChangeDetectionStrategy, Component, EnvironmentInjector, OnDestroy, OnInit } from '@angular/core';
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
  takeUntil,
} from 'rxjs';
import { none, Option, some } from '@kicherkrabbe/shared';
import { ActivatedRoute } from '@angular/router';
import { ButtonSize, Chip, NotificationService } from '../../../../../shared';
import { Embroidery, EmbroideryCategory, EmbroideryCategoryId, ImageId } from '../../model';
import { EmbroideriesService, EmbroideryCategoriesService } from '../../services';
import { environment } from '../../../../../../../environments';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { AssetSelectDialog, AssetSelectDialogData, AssetSelectDialogResult } from '../../../assets/dialogs';
import { AssetsService } from '../../../assets/services/assets.service';

@Component({
  selector: 'app-embroidery-page',
  templateUrl: './embroidery.page.html',
  styleUrls: ['./embroidery.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class EmbroideryPage implements OnInit, OnDestroy {
  private readonly embroideryId$ = new ReplaySubject<string>(1);

  protected readonly embroidery$ = new BehaviorSubject<Option<Embroidery>>(none());
  private readonly loadingEmbroidery$ = new BehaviorSubject<boolean>(false);
  protected readonly embroideryLoaded$ = new BehaviorSubject<boolean>(false);
  protected readonly actionInProgress$ = new BehaviorSubject<boolean>(false);

  protected readonly name$ = new BehaviorSubject<string>('');
  private readonly nameValid$ = this.name$.pipe(map((name) => name.trim().length > 0));
  private readonly nameChanged$ = combineLatest([this.embroidery$, this.name$]).pipe(
    map(([embroidery, name]) => embroidery.map((e) => e.name !== name.trim()).orElse(false)),
  );
  protected readonly cannotSaveName$ = combineLatest([this.nameValid$, this.nameChanged$]).pipe(
    map(([valid, changed]) => !valid || !changed),
  );

  protected readonly availableCategories$ = new BehaviorSubject<EmbroideryCategory[]>([]);
  protected readonly loadingCategories$ = new BehaviorSubject<boolean>(true);
  protected readonly selectedCategories$ = new BehaviorSubject<EmbroideryCategoryId[]>([]);

  protected readonly loading$ = combineLatest([this.loadingEmbroidery$, this.loadingCategories$]).pipe(
    map(([embroideryLoading, categoriesLoading]) => embroideryLoading || categoriesLoading),
  );

  private readonly destroy$ = new Subject<void>();

  protected readonly ButtonSize = ButtonSize;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly embroideriesService: EmbroideriesService,
    private readonly embroideryCategoriesService: EmbroideryCategoriesService,
    private readonly assetsService: AssetsService,
    private readonly notificationService: NotificationService,
    private readonly dialogService: DialogService,
    private readonly environmentInjector: EnvironmentInjector,
  ) {}

  ngOnInit(): void {
    this.reloadAvailableCategories();

    this.route.params
      .pipe(
        map((params) => params['embroideryId']),
        takeUntil(this.destroy$),
      )
      .subscribe((embroideryId) => this.embroideryId$.next(embroideryId));

    this.embroideryId$.pipe(takeUntil(this.destroy$)).subscribe((embroideryId) => this.reloadEmbroidery(embroideryId));

    combineLatest([this.embroidery$, this.selectedCategories$.pipe(debounceTime(300))])
      .pipe(
        filter(([embroidery, categories]) => {
          if (embroidery.isNone()) {
            return false;
          }
          const e = embroidery.orElseThrow();
          return !this.areSetsEqual(e.categories, new Set<EmbroideryCategoryId>(categories));
        }),
        takeUntil(this.destroy$),
      )
      .subscribe(([embroidery, categories]) => this.saveUpdatedCategories(embroidery.orElseThrow(), categories));
  }

  ngOnDestroy(): void {
    this.embroideryId$.complete();

    this.embroidery$.complete();
    this.embroideryLoaded$.complete();
    this.loadingEmbroidery$.complete();
    this.actionInProgress$.complete();
    this.name$.complete();
    this.availableCategories$.complete();
    this.loadingCategories$.complete();
    this.selectedCategories$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateName(value: string): void {
    this.name$.next(value);
  }

  saveName(embroidery: Embroidery): void {
    const name = this.name$.value.trim();
    if (name.length === 0 || name === embroidery.name) {
      return;
    }

    this.embroideriesService
      .renameEmbroidery(embroidery.id, embroidery.version, name)
      .pipe(first())
      .subscribe({
        next: (version) => {
          this.embroidery$.next(some(embroidery.rename(version, name)));

          this.notificationService.publish({
            message: 'Name wurde aktualisiert.',
            type: 'success',
          });
        },
        error: (e) => {
          const reason = e?.error?.reason;
          if (reason === 'ALIAS_ALREADY_IN_USE') {
            this.notificationService.publish({
              type: 'error',
              message: 'Es existiert bereits eine Stickerei mit diesem Namen.',
            });
          } else {
            console.error('Failed to update name', e);
            this.notificationService.publish({
              message: 'Name konnte nicht aktualisiert werden. Bitte versuche es erneut.',
              type: 'error',
            });
          }
        },
      });
  }

  editImage(embroidery: Embroidery): void {
    const dialog = Dialog.create<AssetSelectDialogResult>({
      title: 'Bild auswählen',
      componentType: AssetSelectDialog,
      providers: [
        {
          provide: AssetSelectDialogData,
          useValue: AssetSelectDialogData.of({
            multiple: false,
            watermark: true,
            initialContentTypes: ['image/png', 'image/jpeg'],
          }),
        },
        { provide: AssetsService, useValue: this.assetsService },
      ],
      environmentInjector: this.environmentInjector,
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog.getResult().ifSome((result) => this.saveUpdatedImage(embroidery, result.assetIds[0]));
    });
  }

  publishEmbroidery(embroidery: Embroidery): void {
    this.performLifecycleAction(
      () => this.embroideriesService.publishEmbroidery(embroidery.id, embroidery.version),
      (version) => embroidery.publish(version),
      'Stickerei wurde veröffentlicht.',
      'Stickerei konnte nicht veröffentlicht werden.',
    );
  }

  unpublishEmbroidery(embroidery: Embroidery): void {
    this.performLifecycleAction(
      () => this.embroideriesService.unpublishEmbroidery(embroidery.id, embroidery.version),
      (version) => embroidery.unpublish(version),
      'Veröffentlichung wurde aufgehoben.',
      'Veröffentlichung konnte nicht aufgehoben werden.',
    );
  }

  featureEmbroidery(embroidery: Embroidery): void {
    this.performLifecycleAction(
      () => this.embroideriesService.featureEmbroidery(embroidery.id, embroidery.version),
      (version) => embroidery.feature(version),
      'Stickerei wird nun hervorgehoben.',
      'Stickerei konnte nicht hervorgehoben werden.',
    );
  }

  unfeatureEmbroidery(embroidery: Embroidery): void {
    this.performLifecycleAction(
      () => this.embroideriesService.unfeatureEmbroidery(embroidery.id, embroidery.version),
      (version) => embroidery.unfeature(version),
      'Hervorhebung wurde aufgehoben.',
      'Hervorhebung konnte nicht aufgehoben werden.',
    );
  }

  getImageUrl(imageId: ImageId): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  toCategories(ids: EmbroideryCategoryId[], categories: EmbroideryCategory[]): EmbroideryCategory[] {
    const lookup = new Map<EmbroideryCategoryId, EmbroideryCategory>(categories.map((c) => [c.id, c]));

    return ids
      .map((id) => lookup.get(id))
      .filter((category): category is EmbroideryCategory => !!category)
      .sort((a, b) => a.name.localeCompare(b.name, 'de-de', { numeric: true }));
  }

  categoriesToChips(categories: EmbroideryCategory[]): Chip[] {
    return categories.map((category) => Chip.of({ id: category.id, label: category.name }));
  }

  onCategoryRemoved(chip: Chip): void {
    this.selectedCategories$.next(this.selectedCategories$.value.filter((category) => category !== chip.id));
  }

  onCategoryAdded(chip: Chip): void {
    const category = this.availableCategories$.value.find((c) => c.id === chip.id);
    if (category) {
      this.selectedCategories$.next([...this.selectedCategories$.value, category.id]);
    }
  }

  private performLifecycleAction(
    action: () => Observable<number>,
    update: (version: number) => Embroidery,
    successMessage: string,
    errorMessage: string,
  ): void {
    if (this.actionInProgress$.value) {
      return;
    }
    this.actionInProgress$.next(true);

    action()
      .pipe(
        first(),
        finalize(() => this.actionInProgress$.next(false)),
      )
      .subscribe({
        next: (version) => {
          this.embroidery$.next(some(update(version)));

          this.notificationService.publish({
            message: successMessage,
            type: 'success',
          });
        },
        error: (e) => {
          console.error('Lifecycle action failed', e);
          this.notificationService.publish({
            message: `${errorMessage} Bitte versuche es erneut.`,
            type: 'error',
          });
        },
      });
  }

  private saveUpdatedImage(embroidery: Embroidery, image: ImageId): void {
    if (image === embroidery.image || this.actionInProgress$.value) {
      return;
    }
    this.actionInProgress$.next(true);

    this.embroideriesService
      .updateImage(embroidery.id, embroidery.version, image)
      .pipe(
        first(),
        finalize(() => this.actionInProgress$.next(false)),
      )
      .subscribe({
        next: (version) => {
          this.embroidery$.next(some(embroidery.updateImage(version, image)));

          this.notificationService.publish({
            message: 'Bild wurde aktualisiert.',
            type: 'success',
          });
        },
        error: (e) => {
          console.error('Failed to update image', e);
          this.notificationService.publish({
            message: 'Bild konnte nicht aktualisiert werden. Bitte versuche es erneut.',
            type: 'error',
          });
        },
      });
  }

  private saveUpdatedCategories(embroidery: Embroidery, categories: EmbroideryCategoryId[]): void {
    this.embroideriesService
      .updateCategories(embroidery.id, embroidery.version, categories)
      .pipe(first())
      .subscribe({
        next: (version) => {
          this.embroidery$.next(some(embroidery.updateCategories(version, new Set(categories))));

          this.notificationService.publish({
            message: 'Kategorien wurden aktualisiert.',
            type: 'success',
          });
        },
        error: (e) => {
          console.error('Failed to update categories', e);
          this.notificationService.publish({
            message: 'Kategorien konnten nicht aktualisiert werden. Bitte versuche es erneut.',
            type: 'error',
          });
        },
      });
  }

  private reloadAvailableCategories(): void {
    this.loadingCategories$.next(true);

    this.embroideryCategoriesService
      .getAvailableCategories()
      .pipe(
        first(),
        catchError((e) => {
          console.error('Failed to load categories', e);
          this.notificationService.publish({
            message: 'Kategorien konnten nicht geladen werden. Bitte versuche die Seite neu zu laden.',
            type: 'error',
          });
          return [];
        }),
        finalize(() => this.loadingCategories$.next(false)),
      )
      .subscribe((categories) => this.availableCategories$.next(categories));
  }

  private reloadEmbroidery(embroideryId: string): void {
    if (this.loadingEmbroidery$.value) {
      return;
    }
    this.loadingEmbroidery$.next(true);

    this.embroideriesService
      .getEmbroidery(embroideryId)
      .pipe(
        first(),
        map((embroidery) => some(embroidery)),
        catchError((e) => {
          console.error('Failed to load embroidery', e);
          this.notificationService.publish({
            message: 'Stickerei konnte nicht geladen werden. Bitte versuche die Seite neu zu laden.',
            type: 'error',
          });

          return of(none<Embroidery>());
        }),
        takeUntil(this.destroy$),
        finalize(() => {
          this.loadingEmbroidery$.next(false);
          this.embroideryLoaded$.next(true);
        }),
      )
      .subscribe((embroidery) => {
        this.embroidery$.next(embroidery);
        embroidery.ifSome((e) => {
          this.name$.next(e.name);
          this.selectedCategories$.next(Array.from(e.categories));
        });
      });
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
}
