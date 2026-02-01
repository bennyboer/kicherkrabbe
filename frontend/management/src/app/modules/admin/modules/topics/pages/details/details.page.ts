import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TopicsService } from '../../services';
import { BehaviorSubject, combineLatest, map, Observable, Subject, switchMap, takeUntil } from 'rxjs';
import { Topic } from '../../model';
import { NotificationService } from '../../../../../shared';
import { none, Option, someOrNone } from '../../../../../shared/modules/option';

@Component({
  selector: 'app-topic-details',
  templateUrl: './details.page.html',
  styleUrls: ['./details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class TopicDetailsPage implements OnDestroy {
  private readonly transientName$: BehaviorSubject<Option<string>> = new BehaviorSubject<Option<string>>(none());
  private readonly updatingName$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly failedUpdatingName$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly waitingForDeleteConfirmation$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly topicsService: TopicsService,
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

  getTopic(): Observable<Option<Topic>> {
    return this.getTopicId().pipe(switchMap((id) => this.topicsService.getTopic(id)));
  }

  isLoading(): Observable<boolean> {
    return this.topicsService.isLoading();
  }

  isFailedUpdatingName(): Observable<boolean> {
    return this.failedUpdatingName$.asObservable();
  }

  canUpdateName(): Observable<boolean> {
    return combineLatest([this.transientName$, this.getTopic()]).pipe(
      map(([name, topic]) => {
        if (name.isNone()) {
          return false;
        }

        const n = name.orElse('');
        if (n.length === 0) {
          return false;
        }

        return topic.map((t) => t.name !== n).orElse(false);
      }),
    );
  }

  cannotUpdateName(): Observable<boolean> {
    return this.canUpdateName().pipe(map((can) => !can));
  }

  updateTransientName(name: string): void {
    this.transientName$.next(someOrNone(name.trim()));
  }

  updateName(topic: Topic): void {
    const name = this.transientName$.value.orElseThrow('Name is required');

    this.updatingName$.next(true);
    this.failedUpdatingName$.next(false);
    this.topicsService
      .updateTopicName(topic.id, topic.version, name)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.updatingName$.next(false);
          this.notificationService.publish({
            message: `Das Thema „${name}“ wurde umbenannt.`,
            type: 'success',
          });
        },
        error: () => {
          this.updatingName$.next(false);
          this.failedUpdatingName$.next(true);
        },
      });
  }

  deleteTopic(topic: Topic): void {
    this.topicsService
      .deleteTopic(topic.id, topic.version)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: `Das Thema „${topic.name}“ wurde erfolgreich gelöscht.`,
            type: 'success',
          });
          this.router.navigate(['../'], { relativeTo: this.route });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Ein Fehler ist aufgetreten. Das Thema konnte nicht gelöscht werden. Versuche es noch einmal.',
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

  private getTopicId(): Observable<string> {
    return this.route.paramMap.pipe(map((params) => someOrNone(params.get('id')).orElse('')));
  }
}
