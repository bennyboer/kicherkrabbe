import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import {
  BehaviorSubject,
  combineLatest,
  filter,
  map,
  Observable,
  Subject,
  takeUntil,
} from 'rxjs';
import { FabricsService } from '../../services';
import { Chip, NotificationService } from '../../../../../shared';
import { ActivatedRoute, Router } from '@angular/router';
import { FabricTopic, FabricTypeAvailability } from '../../model';
import { none, Option, some } from '../../../../../../util';
import { environment } from '../../../../../../../environments';

@Component({
  selector: 'app-create-fabric-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreateFabricPage implements AfterViewInit, OnInit, OnDestroy {
  @ViewChild('name')
  nameInput!: ElementRef;

  private readonly name$: BehaviorSubject<string> = new BehaviorSubject<string>(
    '',
  );
  private readonly imageId$: BehaviorSubject<Option<string>> =
    new BehaviorSubject<Option<string>>(none());
  private readonly selectedTopics$: BehaviorSubject<FabricTopic[]> =
    new BehaviorSubject<FabricTopic[]>([]);
  private readonly availableTopics$: BehaviorSubject<FabricTopic[]> =
    new BehaviorSubject<FabricTopic[]>([]);
  private readonly loadingAvailableTopics$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly creatingFabric$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly failed$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly fabricsService: FabricsService,
    private readonly notificationService: NotificationService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) {}

  ngAfterViewInit(): void {
    this.nameInput.nativeElement.focus();
  }

  ngOnInit(): void {
    this.reloadAvailableTopics();
  }

  ngOnDestroy(): void {
    this.name$.complete();
    this.imageId$.complete();
    this.availableTopics$.complete();
    this.loadingAvailableTopics$.complete();
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
    const colors = new Set<string>();
    const topics = new Set<string>(this.selectedTopics$.value.map((t) => t.id));
    const availability: FabricTypeAvailability[] = [];

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
        error: () => {
          this.failed$.next(true);
          this.creatingFabric$.next(false);
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
    return this.name$.pipe(map((name) => name.length > 0));
  }

  canCreateFabric(): Observable<boolean> {
    return combineLatest([this.isFormValid(), this.isCreatingFabric()]).pipe(
      map(([valid, creating]) => valid && !creating),
    );
  }

  cannotCreateFabric(): Observable<boolean> {
    return this.canCreateFabric().pipe(map((canCreate) => !canCreate));
  }

  onImageUploaded(imageId: string): void {
    this.imageId$.next(some(imageId));
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

  getSelectedTopics(): Observable<FabricTopic[]> {
    return this.selectedTopics$.asObservable();
  }

  isLoadingAvailableTopics(): Observable<boolean> {
    return this.loadingAvailableTopics$.asObservable();
  }

  toChips(topics: FabricTopic[]): Chip[] {
    return topics.map(this.toChip);
  }

  onTopicRemoved(chip: Chip) {
    const topics = this.selectedTopics$.value.filter(
      (topic) => topic.id !== chip.id,
    );
    this.selectedTopics$.next(topics);
  }

  onTopicAdded(chip: Chip) {
    const topic = this.availableTopics$.value.find((t) => t.id === chip.id);
    if (topic) {
      const topics = [...this.selectedTopics$.value, topic];
      this.selectedTopics$.next(topics);
    }
  }

  private toChip(topic: FabricTopic): Chip {
    return Chip.of({
      id: topic.id,
      label: topic.name,
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
            message:
              'Die verfügbaren Themen konnten nicht geladen werden. Versuchen Sie die Seite neu zu laden.',
            type: 'error',
          });
        },
      });
  }
}
