import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, catchError, finalize, first, map, Observable, of, Subject } from 'rxjs';
import { NotificationService } from '../../../../../shared';
import { Highlight } from '../../model';
import { HighlightsService } from '../../services';
import { environment } from '../../../../../../../environments';

@Component({
  selector: 'app-highlights-page',
  templateUrl: './highlights.page.html',
  styleUrls: ['./highlights.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class HighlightsPage implements OnInit, OnDestroy {
  protected readonly highlights$ = new BehaviorSubject<Highlight[]>([]);
  private readonly loadingHighlights$ = new BehaviorSubject<boolean>(false);
  protected readonly highlightsLoaded$ = new BehaviorSubject<boolean>(false);

  protected readonly loading$: Observable<boolean> = this.loadingHighlights$;

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly highlightsService: HighlightsService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.loadHighlights();
  }

  ngOnDestroy(): void {
    this.highlights$.complete();
    this.loadingHighlights$.complete();
    this.highlightsLoaded$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }

  private loadHighlights(): void {
    if (this.loadingHighlights$.value) {
      return;
    }
    this.loadingHighlights$.next(true);

    this.highlightsService
      .getHighlights({})
      .pipe(
        first(),
        catchError((e) => {
          console.error('Failed to load highlights', e);
          this.notificationService.publish({
            type: 'error',
            message: 'Die Highlights konnten nicht geladen werden. Bitte versuche die Seite neu zu laden.',
          });
          return of([]);
        }),
        finalize(() => {
          this.loadingHighlights$.next(false);
          this.highlightsLoaded$.next(true);
        }),
      )
      .subscribe((highlights) => this.highlights$.next(highlights));
  }
}
