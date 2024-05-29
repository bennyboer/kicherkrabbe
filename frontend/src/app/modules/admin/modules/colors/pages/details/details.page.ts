import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import {
  BehaviorSubject,
  combineLatest,
  map,
  Observable,
  Subject,
  switchMap,
  takeUntil,
} from 'rxjs';
import { none, Option, someOrNone } from '../../../../../../util';
import { ActivatedRoute, Router } from '@angular/router';
import { ColorsService } from '../../services';
import { NotificationService } from '../../../../../shared';
import { Color } from '../../model';

@Component({
  selector: 'app-color-details-page',
  templateUrl: './details.page.html',
  styleUrls: ['./details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ColorDetailsPage implements OnDestroy {
  private readonly transientName$: BehaviorSubject<Option<string>> =
    new BehaviorSubject<Option<string>>(none());
  private readonly updatingName$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly failedUpdatingName$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly waitingForDeleteConfirmation$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly colorsService: ColorsService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnDestroy(): void {
    this.transientName$.complete();
    this.updatingName$.complete();
    this.failedUpdatingName$.complete();
    this.waitingForDeleteConfirmation$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getColor(): Observable<Option<Color>> {
    return this.getColorId().pipe(
      switchMap((id) => this.colorsService.getColor(id)),
    );
  }

  isLoading(): Observable<boolean> {
    return this.colorsService.isLoading();
  }

  isFailedUpdatingName(): Observable<boolean> {
    return this.failedUpdatingName$.asObservable();
  }

  canUpdateName(): Observable<boolean> {
    return combineLatest([this.transientName$, this.getColor()]).pipe(
      map(([name, color]) => {
        if (name.isNone()) {
          return false;
        }

        const n = name.orElse('');
        if (n.length === 0) {
          return false;
        }

        return color.map((t) => t.name !== n).orElse(false);
      }),
    );
  }

  cannotUpdateName(): Observable<boolean> {
    return this.canUpdateName().pipe(map((can) => !can));
  }

  updateTransientName(name: string): void {
    this.transientName$.next(someOrNone(name.trim()));
  }

  updateName(color: Color): void {
    const name = this.transientName$.value.orElseThrow('Name is required');
    const red = 0;
    const green = 0;
    const blue = 0;
    // TODO Add color to form

    this.updatingName$.next(true);
    this.failedUpdatingName$.next(false);
    this.colorsService
      .updateColor({
        id: color.id,
        version: color.version,
        name,
        red,
        green,
        blue,
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.updatingName$.next(false);
          this.notificationService.publish({
            message: `Die Farbe „${name}“ wurde umbenannt.`,
            type: 'success',
          });
        },
        error: () => {
          this.updatingName$.next(false);
          this.failedUpdatingName$.next(true);
        },
      });
  }

  deleteColor(color: Color): void {
    this.colorsService
      .deleteColor(color.id, color.version)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: `Die Farbe „${color.name}“ wurde erfolgreich gelöscht.`,
            type: 'success',
          });
          this.router.navigate(['../'], { relativeTo: this.route });
        },
        error: () => {
          this.notificationService.publish({
            message:
              'Ein Fehler ist aufgetreten. Die Farbe konnte nicht gelöscht werden. Versuche es noch einmal.',
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

  private getColorId(): Observable<string> {
    return this.route.paramMap.pipe(
      map((params) => someOrNone(params.get('id')).orElse('')),
    );
  }
}
