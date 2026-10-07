import { ChangeDetectionStrategy, Component, EnvironmentInjector, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, catchError, combineLatest, delay, finalize, first, map, Subject, takeUntil } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { none, Option, some } from '@kicherkrabbe/shared';
import { environment } from '../../../../../../../environments';
import { EmbroideryCategory } from '../../model';
import { ButtonSize, Chip, NotificationService } from '../../../../../shared';
import { EmbroideriesService, EmbroideryCategoriesService } from '../../services';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { AssetSelectDialog, AssetSelectDialogData, AssetSelectDialogResult } from '../../../assets/dialogs';
import { AssetsService } from '../../../assets/services/assets.service';

@Component({
  selector: 'app-create-embroidery-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class CreatePage implements OnInit, OnDestroy {
  private readonly name$ = new BehaviorSubject<string>('');
  private readonly nameTouched$ = new BehaviorSubject<boolean>(false);
  private readonly nameValid$ = this.name$.pipe(map((name) => name.length > 0));
  protected readonly nameMissing$ = combineLatest([this.nameTouched$, this.nameValid$]).pipe(
    map(([touched, valid]) => touched && !valid),
  );

  protected readonly imageId$ = new BehaviorSubject<Option<string>>(none());
  private readonly hasImage$ = this.imageId$.pipe(map((id) => id.isSome()));

  protected readonly availableCategories$ = new BehaviorSubject<EmbroideryCategory[]>([]);
  protected readonly loadingCategories$ = new BehaviorSubject<boolean>(true);
  protected readonly selectedCategories$ = new BehaviorSubject<EmbroideryCategory[]>([]);

  protected readonly creating$ = new BehaviorSubject<boolean>(false);
  protected readonly cannotCreate$ = combineLatest([this.nameValid$, this.hasImage$, this.creating$]).pipe(
    map(([nameValid, hasImage, creating]) => !nameValid || !hasImage || creating),
  );

  protected readonly ButtonSize = ButtonSize;

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly embroideriesService: EmbroideriesService,
    private readonly embroideryCategoriesService: EmbroideryCategoriesService,
    private readonly notificationService: NotificationService,
    private readonly dialogService: DialogService,
    private readonly assetsService: AssetsService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly environmentInjector: EnvironmentInjector,
  ) {}

  ngOnInit(): void {
    this.reloadAvailableCategories();
  }

  ngOnDestroy(): void {
    this.name$.complete();
    this.nameTouched$.complete();
    this.imageId$.complete();
    this.availableCategories$.complete();
    this.loadingCategories$.complete();
    this.selectedCategories$.complete();
    this.creating$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateName(value: string): void {
    this.name$.next(value.trim());

    if (!this.nameTouched$.value) {
      this.nameTouched$.next(true);
    }
  }

  selectImage(): void {
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
    this.dialogService
      .waitUntilClosed(dialog.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        dialog.getResult().ifSome((result) => this.imageId$.next(some(result.assetIds[0])));
      });
  }

  clearImage(): void {
    this.imageId$.next(none());
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  categoriesToChips(categories: EmbroideryCategory[]): Chip[] {
    return categories.map((category) => Chip.of({ id: category.id, label: category.name }));
  }

  onCategoryRemoved(chip: Chip): void {
    this.selectedCategories$.next(this.selectedCategories$.value.filter((category) => category.id !== chip.id));
  }

  onCategoryAdded(chip: Chip): void {
    const category = this.availableCategories$.value.find((c) => c.id === chip.id);
    if (category) {
      this.selectedCategories$.next([...this.selectedCategories$.value, category]);
    }
  }

  create(): void {
    const imageId = this.imageId$.value;
    if (imageId.isNone() || this.creating$.value) {
      return;
    }
    this.creating$.next(true);

    this.embroideriesService
      .createEmbroidery({
        name: this.name$.value,
        image: imageId.orElseThrow(),
        categories: this.selectedCategories$.value.map((c) => c.id),
      })
      .pipe(
        delay(500),
        first(),
        finalize(() => this.creating$.next(false)),
      )
      .subscribe({
        next: (embroideryId) => {
          this.notificationService.publish({
            type: 'success',
            message: 'Die Stickerei wurde erstellt.',
          });
          this.router.navigate(['..', embroideryId], { relativeTo: this.route });
        },
        error: (e) => {
          const reason = e?.error?.reason;
          if (reason === 'ALIAS_ALREADY_IN_USE') {
            this.notificationService.publish({
              type: 'error',
              message: 'Es existiert bereits eine Stickerei mit diesem Namen.',
            });
          } else {
            console.error('Failed to create embroidery', e);
            this.notificationService.publish({
              type: 'error',
              message: 'Die Stickerei konnte nicht erstellt werden. Bitte versuche es erneut.',
            });
          }
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
            type: 'error',
            message: 'Kategorien konnten nicht geladen werden. Bitte versuche die Seite neu zu laden.',
          });
          return [];
        }),
        finalize(() => this.loadingCategories$.next(false)),
      )
      .subscribe((categories) => this.availableCategories$.next(categories));
  }
}
