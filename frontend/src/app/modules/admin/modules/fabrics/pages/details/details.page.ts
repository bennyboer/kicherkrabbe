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
import { FabricsService } from '../../services';
import { NotificationService } from '../../../../../shared';
import { Fabric } from '../../model';
import { environment } from '../../../../../../../environments';

@Component({
  selector: 'app-fabric-details-page',
  templateUrl: './details.page.html',
  styleUrls: ['./details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FabricDetailsPage implements OnDestroy {
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
    private readonly fabricsService: FabricsService,
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

  getFabric(): Observable<Option<Fabric>> {
    return this.getFabricId().pipe(
      switchMap((id) => this.fabricsService.getFabric(id)),
    );
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
        error: () => {
          this.updatingName$.next(false);
          this.failedUpdatingName$.next(true);
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
            message:
              'Ein Fehler ist aufgetreten. Der Stoff konnte nicht gelöscht werden. Versuche es noch einmal.',
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

  private getFabricId(): Observable<string> {
    return this.route.paramMap.pipe(
      map((params) => someOrNone(params.get('id')).orElse('')),
    );
  }
}
