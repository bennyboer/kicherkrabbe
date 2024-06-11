import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
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
import { NotificationService } from '../../../../../shared';
import { ActivatedRoute, Router } from '@angular/router';
import { FabricTypeAvailability } from '../../model';
import { none, Option, some } from '../../../../../../util';
import { environment } from '../../../../../../../environments';

@Component({
  selector: 'app-create-fabric-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreateFabricPage implements AfterViewInit, OnDestroy {
  @ViewChild('name')
  nameInput!: ElementRef;

  private readonly name$: BehaviorSubject<string> = new BehaviorSubject<string>(
    '',
  );
  private readonly imageId$: BehaviorSubject<Option<string>> =
    new BehaviorSubject<Option<string>>(none());
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

  ngOnDestroy(): void {
    this.name$.complete();
    this.imageId$.complete();
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
    const topics = new Set<string>();
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
}
