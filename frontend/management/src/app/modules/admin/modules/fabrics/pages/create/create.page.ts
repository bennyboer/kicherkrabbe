import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EnvironmentInjector,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { BehaviorSubject, combineLatest, filter, map, Observable, Subject, takeUntil } from 'rxjs';
import { FabricsService } from '../../services';
import { Chip, ColorBadgeColor, NotificationService } from '../../../../../shared';
import { ActivatedRoute, Router } from '@angular/router';
import { FabricColor, FabricTopic, FabricType, FabricTypeAvailability } from '../../model';
import { environment } from '../../../../../../../environments';
import { none, Option, some } from '@kicherkrabbe/shared';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import {
  AssetSelectDialog,
  AssetSelectDialogData,
  AssetSelectDialogResult,
} from '../../../assets/dialogs';
import { AssetsService } from '../../../assets/services/assets.service';

@Component({
  selector: 'app-create-fabric-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class CreateFabricPage implements AfterViewInit, OnInit, OnDestroy {
  @ViewChild('name')
  nameInput!: ElementRef;

  private readonly name$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  private readonly imageId$: BehaviorSubject<Option<string>> = new BehaviorSubject<Option<string>>(none());
  private readonly selectedTopics$: BehaviorSubject<FabricTopic[]> = new BehaviorSubject<FabricTopic[]>([]);
  private readonly availableTopics$: BehaviorSubject<FabricTopic[]> = new BehaviorSubject<FabricTopic[]>([]);
  private readonly loadingAvailableTopics$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly selectedColors$: BehaviorSubject<FabricColor[]> = new BehaviorSubject<FabricColor[]>([]);
  private readonly availableColors$: BehaviorSubject<FabricColor[]> = new BehaviorSubject<FabricColor[]>([]);
  private readonly loadingAvailableColors$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly selectedFabricTypes$: BehaviorSubject<FabricType[]> = new BehaviorSubject<FabricType[]>([]);
  private readonly availableFabricTypes$: BehaviorSubject<FabricType[]> = new BehaviorSubject<FabricType[]>([]);
  private readonly loadingAvailableFabricTypes$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly creatingFabric$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly failed$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly fabricsService: FabricsService,
    private readonly notificationService: NotificationService,
    private readonly dialogService: DialogService,
    private readonly assetsService: AssetsService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly environmentInjector: EnvironmentInjector,
  ) {}

  ngAfterViewInit(): void {
    this.nameInput.nativeElement.focus();
  }

  ngOnInit(): void {
    this.reloadAvailableTopics();
    this.reloadAvailableColors();
    this.reloadAvailableFabricTypes();
  }

  ngOnDestroy(): void {
    this.name$.complete();
    this.imageId$.complete();
    this.availableTopics$.complete();
    this.loadingAvailableTopics$.complete();
    this.selectedTopics$.complete();
    this.selectedColors$.complete();
    this.availableColors$.complete();
    this.loadingAvailableColors$.complete();
    this.availableFabricTypes$.complete();
    this.selectedFabricTypes$.complete();
    this.loadingAvailableFabricTypes$.complete();
    this.creatingFabric$.complete();
    this.failed$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateName(value: string): void {
    this.name$.next(value.trim());
  }

  createFabric(): boolean {
    const name = this.name$.value;
    const image = this.imageId$.value.orElseThrow('Image ID is missing');
    const colors = new Set<string>(this.selectedColors$.value.map((c) => c.id));
    const topics = new Set<string>(this.selectedTopics$.value.map((t) => t.id));
    const availability: FabricTypeAvailability[] = this.selectedFabricTypes$.value.map((fabricType) =>
      FabricTypeAvailability.of({
        typeId: fabricType.id,
        inStock: true,
      }),
    );

    this.creatingFabric$.next(true);
    this.failed$.next(false);
    this.fabricsService
      .createFabric({
        name,
        image,
        colors,
        topics,
        availability,
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.creatingFabric$.next(false);
          this.notificationService.publish({
            message: `Der Stoff „${name}“ wurde erfolgreich erstellt.`,
            type: 'success',
          });
          this.router.navigate(['..'], { relativeTo: this.route });
        },
        error: (e) => {
          this.creatingFabric$.next(false);
          const reason = e?.error?.reason;
          if (reason === 'ALIAS_ALREADY_IN_USE') {
            this.notificationService.publish({
              message: 'Es existiert bereits ein Stoff mit diesem Namen.',
              type: 'error',
            });
          } else {
            this.failed$.next(true);
          }
        },
      });

    return false;
  }

  isCreatingFabric(): Observable<boolean> {
    return this.creatingFabric$.asObservable();
  }

  isFailed(): Observable<boolean> {
    return this.failed$.asObservable();
  }

  isFormValid(): Observable<boolean> {
    const nameValid$ = this.name$.pipe(map((name) => name.length > 0));
    const imageIdValid$ = this.imageId$.pipe(map((id) => id.isSome()));

    return combineLatest([nameValid$, imageIdValid$]).pipe(
      map(([nameValid, imageIdValid]) => nameValid && imageIdValid),
    );
  }

  canCreateFabric(): Observable<boolean> {
    return combineLatest([this.isFormValid(), this.isCreatingFabric()]).pipe(
      map(([valid, creating]) => valid && !creating),
    );
  }

  cannotCreateFabric(): Observable<boolean> {
    return this.canCreateFabric().pipe(map((canCreate) => !canCreate));
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
        dialog.getResult().ifSome((result) => {
          this.imageId$.next(some(result.assetIds[0]));
        });
      });
  }

  getImageId(): Observable<string> {
    return this.imageId$.pipe(
      filter((id) => id.isSome()),
      map((id) => id.orElseThrow()),
    );
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
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
    return this.selectedTopics$.asObservable();
  }

  getSelectedColors(): Observable<FabricColor[]> {
    return this.selectedColors$.asObservable();
  }

  getSelectedFabricTypes(): Observable<FabricType[]> {
    return this.selectedFabricTypes$.asObservable();
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

  onTopicRemoved(chip: Chip) {
    const topics = this.selectedTopics$.value.filter((topic) => topic.id !== chip.id);
    this.selectedTopics$.next(topics);
  }

  onColorRemoved(chip: Chip) {
    const colors = this.selectedColors$.value.filter((color) => color.id !== chip.id);
    this.selectedColors$.next(colors);
  }

  onFabricTypeRemoved(chip: Chip) {
    const fabricTypes = this.selectedFabricTypes$.value.filter((fabricType) => fabricType.id !== chip.id);
    this.selectedFabricTypes$.next(fabricTypes);
  }

  onTopicAdded(chip: Chip) {
    const topic = this.availableTopics$.value.find((t) => t.id === chip.id);
    if (topic) {
      const topics = [...this.selectedTopics$.value, topic];
      this.selectedTopics$.next(topics);
    }
  }

  onColorAdded(chip: Chip) {
    const color = this.availableColors$.value.find((t) => t.id === chip.id);
    if (color) {
      const colors = [...this.selectedColors$.value, color];
      this.selectedColors$.next(colors);
    }
  }

  onFabricTypeAdded(chip: Chip) {
    const fabricType = this.availableFabricTypes$.value.find((t) => t.id === chip.id);
    if (fabricType) {
      const fabricTypes = [...this.selectedFabricTypes$.value, fabricType];
      this.selectedFabricTypes$.next(fabricTypes);
    }
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
