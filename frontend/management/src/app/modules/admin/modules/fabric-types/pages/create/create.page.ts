import { AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable, Subject, takeUntil } from 'rxjs';
import { NotificationService } from '../../../../../shared';
import { ActivatedRoute, Router } from '@angular/router';
import { FabricTypesService } from '../../services';

@Component({
  selector: 'app-create-fabric-type-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class CreateFabricTypePage implements AfterViewInit, OnDestroy {
  @ViewChild('name')
  nameInput!: ElementRef;

  private readonly name$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  private readonly creatingFabricType$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly failed$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly fabricTypesService: FabricTypesService,
    private readonly notificationService: NotificationService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) {}

  ngAfterViewInit(): void {
    this.nameInput.nativeElement.focus();
  }

  ngOnDestroy(): void {
    this.name$.complete();
    this.creatingFabricType$.complete();
    this.failed$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateName(value: string): void {
    this.name$.next(value.trim());
  }

  createFabricType(): boolean {
    const name = this.name$.value;

    this.creatingFabricType$.next(true);
    this.failed$.next(false);
    this.fabricTypesService
      .createFabricType(name)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.creatingFabricType$.next(false);
          this.notificationService.publish({
            message: `Die Stoffart „${name}“ wurde erfolgreich erstellt.`,
            type: 'success',
          });
          this.router.navigate(['..'], { relativeTo: this.route });
        },
        error: () => {
          this.failed$.next(true);
          this.creatingFabricType$.next(false);
        },
      });

    return false;
  }

  isCreatingFabricType(): Observable<boolean> {
    return this.creatingFabricType$.asObservable();
  }

  isFailed(): Observable<boolean> {
    return this.failed$.asObservable();
  }

  isFormValid(): Observable<boolean> {
    return this.name$.pipe(map((name) => name.length > 0));
  }

  canCreateFabricType(): Observable<boolean> {
    return combineLatest([this.isFormValid(), this.isCreatingFabricType()]).pipe(
      map(([valid, creating]) => valid && !creating),
    );
  }

  cannotCreateFabricType(): Observable<boolean> {
    return this.canCreateFabricType().pipe(map((canCreate) => !canCreate));
  }
}
