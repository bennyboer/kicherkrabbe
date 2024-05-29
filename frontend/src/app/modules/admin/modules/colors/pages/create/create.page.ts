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
  map,
  Observable,
  Subject,
  takeUntil,
} from 'rxjs';
import { ColorsService } from '../../services';
import { NotificationService } from '../../../../../shared';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-create-color-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreateColorPage implements AfterViewInit, OnDestroy {
  @ViewChild('name')
  nameInput!: ElementRef;

  private readonly name$: BehaviorSubject<string> = new BehaviorSubject<string>(
    '',
  );
  private readonly creatingColor$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly failed$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly colorsService: ColorsService,
    private readonly notificationService: NotificationService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) {}

  ngAfterViewInit(): void {
    this.nameInput.nativeElement.focus();
  }

  ngOnDestroy(): void {
    this.name$.complete();
    this.creatingColor$.complete();
    this.failed$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateName(value: string): void {
    this.name$.next(value.trim());
  }

  createColor(): boolean {
    const name = this.name$.value;
    const red = 0;
    const green = 0;
    const blue = 0;
    // TODO Add color to form

    this.creatingColor$.next(true);
    this.failed$.next(false);
    this.colorsService
      .createColor({ name, red, green, blue })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.creatingColor$.next(false);
          this.notificationService.publish({
            message: `Die Farbe „${name}“ wurde erfolgreich erstellt.`,
            type: 'success',
          });
          this.router.navigate(['..'], { relativeTo: this.route });
        },
        error: () => {
          this.failed$.next(true);
          this.creatingColor$.next(false);
        },
      });

    return false;
  }

  isCreatingColor(): Observable<boolean> {
    return this.creatingColor$.asObservable();
  }

  isFailed(): Observable<boolean> {
    return this.failed$.asObservable();
  }

  isFormValid(): Observable<boolean> {
    return this.name$.pipe(map((name) => name.length > 0));
  }

  canCreateColor(): Observable<boolean> {
    return combineLatest([this.isFormValid(), this.isCreatingColor()]).pipe(
      map(([valid, creating]) => valid && !creating),
    );
  }

  cannotCreateColor(): Observable<boolean> {
    return this.canCreateColor().pipe(map((canCreate) => !canCreate));
  }
}
