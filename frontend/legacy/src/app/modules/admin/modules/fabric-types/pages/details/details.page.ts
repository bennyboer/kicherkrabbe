import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable, Subject, switchMap, takeUntil } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationService } from '../../../../../shared';
import { FabricTypesService } from '../../services';
import { FabricType } from '../../model';
import { none, Option, someOrNone } from '../../../../../shared/modules/option';

@Component({
  selector: 'app-fabric-type-details-page',
  templateUrl: './details.page.html',
  styleUrls: ['./details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class FabricTypeDetailsPage implements OnDestroy {
  private readonly transientName$: BehaviorSubject<Option<string>> = new BehaviorSubject<Option<string>>(none());
  private readonly updatingName$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly failedUpdatingName$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly waitingForDeleteConfirmation$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly fabricTypesService: FabricTypesService,
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

  getFabricType(): Observable<Option<FabricType>> {
    return this.getFabricTypeId().pipe(switchMap((id) => this.fabricTypesService.getFabricType(id)));
  }

  isLoading(): Observable<boolean> {
    return this.fabricTypesService.isLoading();
  }

  isFailedUpdatingName(): Observable<boolean> {
    return this.failedUpdatingName$.asObservable();
  }

  canUpdateName(): Observable<boolean> {
    return combineLatest([this.transientName$, this.getFabricType()]).pipe(
      map(([name, fabricType]) => {
        if (name.isNone()) {
          return false;
        }

        const n = name.orElse('');
        if (n.length === 0) {
          return false;
        }

        return fabricType.map((t) => t.name !== n).orElse(false);
      }),
    );
  }

  cannotUpdateName(): Observable<boolean> {
    return this.canUpdateName().pipe(map((can) => !can));
  }

  updateTransientName(name: string): void {
    this.transientName$.next(someOrNone(name.trim()));
  }

  updateName(fabricType: FabricType): void {
    const name = this.transientName$.value.orElseThrow('Name is required');

    this.updatingName$.next(true);
    this.failedUpdatingName$.next(false);
    this.fabricTypesService
      .updateFabricTypeName(fabricType.id, fabricType.version, name)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.updatingName$.next(false);
          this.notificationService.publish({
            message: `Die Stoffart „${name}“ wurde umbenannt.`,
            type: 'success',
          });
        },
        error: () => {
          this.updatingName$.next(false);
          this.failedUpdatingName$.next(true);
        },
      });
  }

  deleteFabricType(fabricType: FabricType): void {
    this.fabricTypesService
      .deleteFabricType(fabricType.id, fabricType.version)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: `Die Stoffart „${fabricType.name}“ wurde erfolgreich gelöscht.`,
            type: 'success',
          });
          this.router.navigate(['../'], { relativeTo: this.route });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Ein Fehler ist aufgetreten. Die Stoffart konnte nicht gelöscht werden. Versuche es noch einmal.',
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

  private getFabricTypeId(): Observable<string> {
    return this.route.paramMap.pipe(map((params) => someOrNone(params.get('id')).orElse('')));
  }
}
