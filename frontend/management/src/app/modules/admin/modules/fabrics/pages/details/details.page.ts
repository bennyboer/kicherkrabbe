import { ChangeDetectionStrategy, Component, Injector, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, combineLatest, filter, map, Observable, Subject, switchMap, takeUntil } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { FabricsService } from '../../services';
import { Chip, ColorBadgeColor, NotificationService } from '../../../../../shared';
import { Fabric, FabricColor, FabricTopic, FabricType, FabricTypeAvailability } from '../../model';
import { environment } from '../../../../../../../environments';
import { none, Option, some, someOrNone } from '@kicherkrabbe/shared';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import {
  AssetSelectDialog,
  AssetSelectDialogData,
  AssetSelectDialogResult,
} from '../../../assets/dialogs';
import { AssetsService } from '../../../assets/services/assets.service';

@Component({
  selector: 'app-fabric-details-page',
  templateUrl: './details.page.html',
  styleUrls: ['./details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class FabricDetailsPage implements OnInit, OnDestroy {
  private readonly transientName$: BehaviorSubject<Option<string>> = new BehaviorSubject<Option<string>>(none());
  private readonly updatingName$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly failedUpdatingName$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly waitingForDeleteConfirmation$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly imageId$: BehaviorSubject<Option<string>> = new BehaviorSubject<Option<string>>(none());
  private readonly availableTopics$: BehaviorSubject<FabricTopic[]> = new BehaviorSubject<FabricTopic[]>([]);
  private readonly loadingAvailableTopics$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly availableColors$: BehaviorSubject<FabricColor[]> = new BehaviorSubject<FabricColor[]>([]);
  private readonly loadingAvailableColors$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly availableFabricTypes$: BehaviorSubject<FabricType[]> = new BehaviorSubject<FabricType[]>([]);
  private readonly loadingAvailableFabricTypes$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly fabricsService: FabricsService,
    private readonly notificationService: NotificationService,
    private readonly dialogService: DialogService,
    private readonly assetsService: AssetsService,
  ) {}

  ngOnInit(): void {
    this.reloadAvailableTopics();
    this.reloadAvailableColors();
    this.reloadAvailableFabricTypes();
  }

  ngOnDestroy(): void {
    this.transientName$.complete();
    this.updatingName$.complete();
    this.failedUpdatingName$.complete();
    this.waitingForDeleteConfirmation$.complete();
    this.imageId$.complete();
    this.availableTopics$.complete();
    this.loadingAvailableTopics$.complete();
    this.availableColors$.complete();
    this.loadingAvailableColors$.complete();
    this.availableFabricTypes$.complete();
    this.loadingAvailableFabricTypes$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getFabric(): Observable<Option<Fabric>> {
    return this.getFabricId().pipe(switchMap((id) => this.fabricsService.getFabric(id)));
  }

  isLoading(): Observable<boolean> {
    return this.fabricsService.isLoading();
  }

  isFailedUpdatingName(): Observable<boolean> {
    return this.failedUpdatingName$.asObservable();
  }

  canUpdateName(): Observable<boolean> {
    return combineLatest([this.transientName$, this.getFabric()]).pipe(
      map(([name, fabric]) => {
        if (name.isNone()) {
          return false;
        }

        const n = name.orElse('');
        if (n.length === 0) {
          return false;
        }

        return fabric.map((t) => t.name !== n).orElse(false);
      }),
    );
  }

  cannotUpdateName(): Observable<boolean> {
    return this.canUpdateName().pipe(map((can) => !can));
  }

  updateTransientName(name: string): void {
    this.transientName$.next(someOrNone(name.trim()));
  }

  updateName(fabric: Fabric): void {
    const name = this.transientName$.value.orElseThrow('Name is required');

    this.updatingName$.next(true);
    this.failedUpdatingName$.next(false);
    this.fabricsService
      .updateFabricName(fabric.id, fabric.version, name)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.updatingName$.next(false);
          this.notificationService.publish({
            message: `Der Stoff „${name}“ wurde umbenannt.`,
            type: 'success',
          });
        },
        error: (e) => {
          this.updatingName$.next(false);
          const reason = e?.error?.reason;
          if (reason === 'ALIAS_ALREADY_IN_USE') {
            this.notificationService.publish({
              message: 'Es existiert bereits ein Stoff mit diesem Namen.',
              type: 'error',
            });
          } else {
            this.failedUpdatingName$.next(true);
          }
        },
      });
  }

  updateImage(fabric: Fabric, imageId: string): void {
    this.fabricsService
      .updateFabricImage(fabric.id, fabric.version, imageId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: `Das Bild wurde erfolgreich aktualisiert.`,
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Ein Fehler ist aufgetreten. Das Bild konnte nicht aktualisiert werden. Versuche es noch einmal.',
            type: 'error',
          });
        },
      });
  }

  deleteFabric(fabric: Fabric): void {
    this.fabricsService
      .deleteFabric(fabric.id, fabric.version)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: `Der Stoff „${fabric.name}“ wurde erfolgreich gelöscht.`,
            type: 'success',
          });
          this.router.navigate(['../'], { relativeTo: this.route });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Ein Fehler ist aufgetreten. Der Stoff konnte nicht gelöscht werden. Versuche es noch einmal.',
            type: 'error',
          });
        },
      });
  }

  publishFabric(fabric: Fabric): void {
    this.fabricsService
      .publishFabric(fabric.id, fabric.version)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: `Der Stoff „${fabric.name}“ wurde erfolgreich veröffentlicht.`,
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message:
              'Ein Fehler ist aufgetreten. Der Stoff konnte nicht veröffentlicht werden. Versuche es noch einmal.',
            type: 'error',
          });
        },
      });
  }

  unpublishFabric(fabric: Fabric): void {
    this.fabricsService
      .unpublishFabric(fabric.id, fabric.version)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: `Der Stoff „${fabric.name}" wurde erfolgreich von der Öffentlichkeit zurückgezogen.`,
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message:
              'Ein Fehler ist aufgetreten. Der Stoff konnte nicht von der Öffentlichkeit zurückgezogen werden. Versuche es noch einmal.',
            type: 'error',
          });
        },
      });
  }

  featureFabric(fabric: Fabric): void {
    this.fabricsService
      .featureFabric(fabric.id, fabric.version)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: `Der Stoff „${fabric.name}" wurde hervorgehoben.`,
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Ein Fehler ist aufgetreten. Der Stoff konnte nicht hervorgehoben werden. Versuche es noch einmal.',
            type: 'error',
          });
        },
      });
  }

  unfeatureFabric(fabric: Fabric): void {
    this.fabricsService
      .unfeatureFabric(fabric.id, fabric.version)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: `Der Stoff „${fabric.name}" wird nicht mehr hervorgehoben.`,
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message:
              'Ein Fehler ist aufgetreten. Die Hervorhebung des Stoffs konnte nicht aufgehoben werden. Versuche es noch einmal.',
            type: 'error',
          });
        },
      });
  }

  isDeleteConfirmation(): Observable<boolean> {
    return this.waitingForDeleteConfirmation$.asObservable();
  }

  waitForDeleteConfirmation(): void {
    this.waitingForDeleteConfirmation$.next(true);
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  selectImage(fabric: Fabric): void {
    const dialog = Dialog.create<AssetSelectDialogResult>({
      title: 'Bild auswählen',
      componentType: AssetSelectDialog,
      injector: Injector.create({
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
      }),
    });
    this.dialogService.open(dialog);
    this.dialogService
      .waitUntilClosed(dialog.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        dialog.getResult().ifSome((result) => {
          this.imageId$.next(some(result.assetIds[0]));
          this.updateImage(fabric, result.assetIds[0]);
        });
      });
  }

  getImageId(): Observable<string> {
    return this.imageId$.pipe(
      filter((id) => id.isSome()),
      map((id) => id.orElseThrow()),
    );
  }

  getAvailableTopics(): Observable<FabricTopic[]> {
    return this.availableTopics$.asObservable();
  }

  getAvailableColors(): Observable<FabricColor[]> {
    return this.availableColors$.asObservable();
  }

  getAvailableFabricTypes(): Observable<FabricType[]> {
    return this.availableFabricTypes$.asObservable();
  }

  getSelectedTopics(): Observable<FabricTopic[]> {
    return combineLatest([this.getFabric(), this.getAvailableTopics()]).pipe(
      map(([fabric, availableTopics]) => {
        const selectedTopicIds = fabric.map((f) => f.topics).orElse(new Set<string>());

        return availableTopics.filter((topic) => selectedTopicIds.has(topic.id));
      }),
    );
  }

  getSelectedColors(): Observable<FabricColor[]> {
    return combineLatest([this.getFabric(), this.getAvailableColors()]).pipe(
      map(([fabric, availableColors]) => {
        const selectedColorIds = fabric.map((f) => f.colors).orElse(new Set<string>());

        return availableColors.filter((color) => selectedColorIds.has(color.id));
      }),
    );
  }

  getSelectedFabricTypes(): Observable<FabricType[]> {
    return combineLatest([this.getFabric(), this.getAvailableFabricTypes()]).pipe(
      map(([fabric, availableFabricTypes]) => {
        const selectedFabricTypeIds = new Set<string>();
        for (const availability of fabric.map((f) => f.availability).orElse([])) {
          selectedFabricTypeIds.add(availability.typeId);
        }

        return availableFabricTypes.filter((fabricType) => selectedFabricTypeIds.has(fabricType.id));
      }),
    );
  }

  isLoadingAvailableTopics(): Observable<boolean> {
    return this.loadingAvailableTopics$.asObservable();
  }

  isLoadingAvailableColors(): Observable<boolean> {
    return this.loadingAvailableColors$.asObservable();
  }

  isLoadingAvailableFabricTypes(): Observable<boolean> {
    return this.loadingAvailableFabricTypes$.asObservable();
  }

  topicsToChips(topics: FabricTopic[]): Chip[] {
    return topics.map(this.topicToChip);
  }

  colorsToChips(colors: FabricColor[]): Chip[] {
    return colors.map(this.colorToChip);
  }

  fabricTypesToChips(fabricTypes: FabricType[]): Chip[] {
    return fabricTypes.map(this.fabricTypeToChip);
  }

  onTopicRemoved(fabric: Fabric, selectedTopics: FabricTopic[], chip: Chip) {
    const updatedSelectedTopics = new Set<string>(
      selectedTopics.filter((topic) => topic.id !== chip.id).map((topic) => topic.id),
    );

    this.fabricsService
      .updateFabricTopics(fabric.id, fabric.version, updatedSelectedTopics)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: 'Das Thema wurde entfernt.',
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Das Thema konnte nicht entfernt werden. Versuchen Sie es noch einmal.',
            type: 'error',
          });
        },
      });
  }

  onColorRemoved(fabric: Fabric, selectedColors: FabricColor[], chip: Chip) {
    const updatedSelectedColors = new Set<string>(
      selectedColors.filter((color) => color.id !== chip.id).map((color) => color.id),
    );

    this.fabricsService
      .updateFabricColors(fabric.id, fabric.version, updatedSelectedColors)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: 'Die Farbe wurde entfernt.',
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Die Farbe konnte nicht entfernt werden. Versuchen Sie es noch einmal.',
            type: 'error',
          });
        },
      });
  }

  onFabricTypeRemoved(fabric: Fabric, selectedFabricTypes: FabricType[], chip: Chip) {
    const updatedSelectedFabricTypes = selectedFabricTypes
      .filter((type) => type.id !== chip.id)
      .map((type) => FabricTypeAvailability.of({ typeId: type.id, inStock: true }));

    this.fabricsService
      .updateFabricAvailability(fabric.id, fabric.version, updatedSelectedFabricTypes)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: 'Die Stoffart wurde entfernt.',
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Die Stoffart konnte nicht entfernt werden. Versuchen Sie es noch einmal.',
            type: 'error',
          });
        },
      });
  }

  onTopicAdded(fabric: Fabric, selectedTopics: FabricTopic[], chip: Chip) {
    const updatedSelectedTopics = new Set<string>(selectedTopics.map((topic) => topic.id));
    updatedSelectedTopics.add(chip.id);

    this.fabricsService
      .updateFabricTopics(fabric.id, fabric.version, updatedSelectedTopics)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: 'Das Thema wurde hinzugefügt.',
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Das Thema konnte nicht hinzugefügt werden. Versuchen Sie es noch einmal.',
            type: 'error',
          });
        },
      });
  }

  onColorAdded(fabric: Fabric, selectedColors: FabricColor[], chip: Chip) {
    const updatedSelectedColors = new Set<string>(selectedColors.map((color) => color.id));
    updatedSelectedColors.add(chip.id);

    this.fabricsService
      .updateFabricColors(fabric.id, fabric.version, updatedSelectedColors)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: 'Die Farbe wurde hinzugefügt.',
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Die Farbe konnte nicht hinzugefügt werden. Versuchen Sie es noch einmal.',
            type: 'error',
          });
        },
      });
  }

  onFabricTypeAdded(fabric: Fabric, selectedFabricTypes: FabricType[], chip: Chip) {
    const updatedSelectedFabricTypes = selectedFabricTypes.map((type) =>
      FabricTypeAvailability.of({ typeId: type.id, inStock: true }),
    );
    updatedSelectedFabricTypes.push(
      FabricTypeAvailability.of({
        typeId: chip.id,
        inStock: true,
      }),
    );

    this.fabricsService
      .updateFabricAvailability(fabric.id, fabric.version, updatedSelectedFabricTypes)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: 'Die Stoffart wurde hinzugefügt.',
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Die Stoffart konnte nicht hinzugefügt werden. Versuchen Sie es noch einmal.',
            type: 'error',
          });
        },
      });
  }

  private getFabricId(): Observable<string> {
    return this.route.paramMap.pipe(map((params) => someOrNone(params.get('id')).orElse('')));
  }

  private topicToChip(topic: FabricTopic): Chip {
    return Chip.of({
      id: topic.id,
      label: topic.name,
    });
  }

  private colorToChip(color: FabricColor): Chip {
    const colorBadgeColor: ColorBadgeColor = {
      red: color.red,
      green: color.green,
      blue: color.blue,
    };

    return Chip.of({
      id: color.id,
      label: color.name,
      content: colorBadgeColor,
    });
  }

  private fabricTypeToChip(fabricType: FabricType): Chip {
    return Chip.of({
      id: fabricType.id,
      label: fabricType.name,
    });
  }

  private reloadAvailableTopics(): void {
    this.loadingAvailableTopics$.next(true);
    this.fabricsService
      .getAvailableTopicsForFabrics()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (topics) => {
          this.availableTopics$.next(topics);
          this.loadingAvailableTopics$.next(false);
        },
        error: () => {
          this.loadingAvailableTopics$.next(false);
          this.notificationService.publish({
            message: 'Die verfügbaren Themen konnten nicht geladen werden. Versuchen Sie die Seite neu zu laden.',
            type: 'error',
          });
        },
      });
  }

  private reloadAvailableColors(): void {
    this.loadingAvailableColors$.next(true);
    this.fabricsService
      .getAvailableColorsForFabrics()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (colors) => {
          this.availableColors$.next(colors);
          this.loadingAvailableColors$.next(false);
        },
        error: () => {
          this.loadingAvailableColors$.next(false);
          this.notificationService.publish({
            message: 'Die verfügbaren Farben konnten nicht geladen werden. Versuchen Sie die Seite neu zu laden.',
            type: 'error',
          });
        },
      });
  }

  private reloadAvailableFabricTypes(): void {
    this.loadingAvailableFabricTypes$.next(true);
    this.fabricsService
      .getAvailableFabricTypesForFabrics()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (fabricTypes) => {
          this.availableFabricTypes$.next(fabricTypes);
          this.loadingAvailableFabricTypes$.next(false);
        },
        error: () => {
          this.loadingAvailableFabricTypes$.next(false);
          this.notificationService.publish({
            message: 'Die verfügbaren Stoffarten konnten nicht geladen werden. Versuchen Sie die Seite neu zu laden.',
            type: 'error',
          });
        },
      });
  }
}
