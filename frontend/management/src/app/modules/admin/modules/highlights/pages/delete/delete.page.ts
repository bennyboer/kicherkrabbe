import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, combineLatest, delay, finalize, first, map, ReplaySubject, Subject, takeUntil } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationService } from '../../../../../shared';
import { HighlightsService } from '../../services';
import { Highlight } from '../../model';

@Component({
  selector: 'app-delete-page',
  templateUrl: './delete.page.html',
  styleUrls: ['./delete.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class DeletePage implements OnInit, OnDestroy {
  protected readonly highlightId$ = new ReplaySubject<string>(1);
  protected readonly highlight$ = new ReplaySubject<Highlight>(1);
  protected readonly loadingHighlight$ = new BehaviorSubject<boolean>(false);
  protected readonly deleting$ = new BehaviorSubject<boolean>(false);

  protected readonly loading$ = combineLatest([this.loadingHighlight$, this.deleting$]).pipe(
    map(([loadingHighlight, deleting]) => loadingHighlight || deleting),
  );

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly highlightsService: HighlightsService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.route.params
      .pipe(
        map((params) => params['highlightId']),
        takeUntil(this.destroy$),
      )
      .subscribe((highlightId) => this.highlightId$.next(highlightId));

    this.highlightId$.pipe(takeUntil(this.destroy$)).subscribe((highlightId) => this.reloadHighlight(highlightId));
  }

  ngOnDestroy(): void {
    this.highlightId$.complete();
    this.highlight$.complete();
    this.loadingHighlight$.complete();
    this.deleting$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  deleteHighlight(highlight: Highlight): void {
    if (this.deleting$.value) {
      return;
    }
    this.deleting$.next(true);

    this.highlightsService
      .deleteHighlight(highlight.id, highlight.version)
      .pipe(
        delay(500),
        finalize(() => this.deleting$.next(false)),
      )
      .subscribe({
        next: () => {
          this.notificationService.publish({
            type: 'success',
            message: 'Das Highlight wurde gelöscht.',
          });
          this.router.navigate(['../..'], { relativeTo: this.route });
        },
        error: () => {
          this.notificationService.publish({
            type: 'error',
            message: 'Das Highlight konnte nicht gelöscht werden.',
          });
        },
      });
  }

  private reloadHighlight(highlightId: string): void {
    if (this.loadingHighlight$.value) {
      return;
    }
    this.loadingHighlight$.next(true);

    this.highlightsService
      .getHighlight(highlightId)
      .pipe(
        first(),
        finalize(() => this.loadingHighlight$.next(false)),
      )
      .subscribe((highlight) => this.highlight$.next(highlight));
  }
}
