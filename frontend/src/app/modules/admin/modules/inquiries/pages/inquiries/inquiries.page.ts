import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import {
  BehaviorSubject,
  catchError,
  combineLatest,
  EMPTY,
  finalize,
  first,
  map,
  Observable,
  Subject,
  takeUntil,
} from 'rxjs';
import { NotificationService } from '../../../../../shared';
import { InquiriesService } from '../../services';
import { RateLimit, RateLimits, Statistics } from '../../models';

@Component({
  selector: 'app-inquiries-page',
  templateUrl: './inquiries.page.html',
  styleUrls: ['./inquiries.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InquiriesPage implements OnInit, OnDestroy {
  private readonly loadingStats$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
  protected readonly statsLoaded$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly stats$: BehaviorSubject<Statistics[]> = new BehaviorSubject<Statistics[]>([]);
  private readonly loadingSettings$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
  protected readonly settingsLoaded$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly loading$: Observable<boolean> = combineLatest([this.loadingStats$, this.loadingSettings$]).pipe(
    map(([loadingStats, loadingSettings]) => loadingStats || loadingSettings),
  );

  protected readonly contactFormEnabled$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly updatingContactFormEnabled$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  protected readonly rateLimitForMail$: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  protected readonly pendingRateLimitForMail$: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  protected readonly updatingRateLimitForMail$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly cannotUpdateRateLimitForMail$: Observable<boolean> = combineLatest([
    this.rateLimitForMail$,
    this.pendingRateLimitForMail$,
    this.updatingRateLimitForMail$,
  ]).pipe(
    map(
      ([rateLimitForMail, pendingRateLimitForMail, updatingRateLimitForMail]) =>
        rateLimitForMail === pendingRateLimitForMail || updatingRateLimitForMail,
    ),
  );

  protected readonly rateLimitForIp$: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  protected readonly pendingRateLimitForIp$: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  protected readonly updatingRateLimitForIp$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly cannotUpdateRateLimitForIp$: Observable<boolean> = combineLatest([
    this.rateLimitForIp$,
    this.pendingRateLimitForIp$,
    this.updatingRateLimitForIp$,
  ]).pipe(
    map(
      ([rateLimitForIp, pendingRateLimitForIp, updatingRateLimitForIp]) =>
        rateLimitForIp === pendingRateLimitForIp || updatingRateLimitForIp,
    ),
  );

  protected readonly rateLimitPerDay$: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  protected readonly pendingRateLimitPerDay$: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  protected readonly updatingRateLimitPerDay$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly cannotUpdateRateLimitPerDay$: Observable<boolean> = combineLatest([
    this.rateLimitPerDay$,
    this.pendingRateLimitPerDay$,
    this.updatingRateLimitPerDay$,
  ]).pipe(
    map(
      ([rateLimitPerDay, pendingRateLimitPerDay, updatingRateLimitPerDay]) =>
        rateLimitPerDay === pendingRateLimitPerDay || updatingRateLimitPerDay,
    ),
  );

  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly notificationService: NotificationService,
    private readonly inquiriesService: InquiriesService,
  ) {}

  ngOnInit(): void {
    this.reloadStats();
    this.reloadSettings();
  }

  ngOnDestroy(): void {
    this.loadingStats$.complete();
    this.statsLoaded$.complete();
    this.stats$.complete();

    this.loadingSettings$.complete();
    this.settingsLoaded$.complete();

    this.contactFormEnabled$.complete();
    this.updatingContactFormEnabled$.complete();

    this.rateLimitForMail$.complete();
    this.pendingRateLimitForMail$.complete();
    this.updatingRateLimitForMail$.complete();

    this.rateLimitForIp$.complete();
    this.pendingRateLimitForIp$.complete();
    this.updatingRateLimitForIp$.complete();

    this.rateLimitPerDay$.complete();
    this.pendingRateLimitPerDay$.complete();
    this.updatingRateLimitPerDay$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  toggleContactFormEnabled(): void {
    this.updatingContactFormEnabled$.next(true);

    const isActivating = !this.contactFormEnabled$.value;

    const action$ = isActivating ? this.inquiriesService.enableInquiries() : this.inquiriesService.disableInquiries();

    action$
      .pipe(
        first(),
        catchError((e) => {
          console.error('Failed to toggle contact form enabled', e);
          this.notificationService.publish({
            message: 'Das Kontaktformular konnte nicht aktiviert/deaktiviert werden',
            type: 'error',
          });
          return EMPTY;
        }),
        finalize(() => {
          this.updatingContactFormEnabled$.next(false);
          this.notificationService.publish({
            type: 'success',
            message: isActivating
              ? 'Das Kontaktformular wurde erfolgreich aktiviert'
              : 'Das Kontaktformular wurde erfolgreich deaktiviert',
          });
        }),
        takeUntil(this.destroy$),
      )
      .subscribe(() => this.contactFormEnabled$.next(!this.contactFormEnabled$.value));
  }

  updatePendingRateLimitForMail(rateLimit: string): void {
    const rateLimitNumber = parseInt(rateLimit, 10);
    if (isNaN(rateLimitNumber)) {
      return;
    }

    this.pendingRateLimitForMail$.next(rateLimitNumber);
  }

  updateRateLimitForMail(rateLimit: string): void {
    const rateLimitNumber = parseInt(rateLimit, 10);
    if (isNaN(rateLimitNumber)) {
      return;
    }

    const currentRateLimit = this.rateLimitForMail$.value;
    if (rateLimitNumber === currentRateLimit) {
      this.notificationService.publish({
        message: 'Das Limit für Kontaktanfragen je E-Mail Adresse ist bereits auf diesen Wert eingestellt',
        type: 'warn',
      });
      return;
    }

    this.updatingRateLimitForMail$.next(true);

    const rateLimits = RateLimits.of({
      perMail: RateLimit.fullDay(rateLimitNumber),
      perIp: RateLimit.fullDay(this.rateLimitForIp$.value),
      overall: RateLimit.fullDay(this.rateLimitPerDay$.value),
    });

    this.inquiriesService
      .updateRateLimits(rateLimits)
      .pipe(
        first(),
        catchError((e) => {
          console.error('Failed to update rate limit for mail', e);
          this.notificationService.publish({
            message: 'Das Limit für Kontaktanfragen je E-Mail Adresse konnte nicht aktualisiert werden',
            type: 'error',
          });
          return EMPTY;
        }),
        finalize(() => {
          this.updatingRateLimitForMail$.next(false);
          this.notificationService.publish({
            type: 'success',
            message: `Das Limit für Kontaktanfragen je E-Mail Adresse wurde erfolgreich auf ${rateLimitNumber} aktualisiert`,
          });
        }),
        takeUntil(this.destroy$),
      )
      .subscribe(() => this.rateLimitForMail$.next(rateLimitNumber));
  }

  updatePendingRateLimitForIp(rateLimit: string): void {
    const rateLimitNumber = parseInt(rateLimit, 10);
    if (isNaN(rateLimitNumber)) {
      return;
    }

    this.pendingRateLimitForIp$.next(rateLimitNumber);
  }

  updateRateLimitForIp(rateLimit: string): void {
    const rateLimitNumber = parseInt(rateLimit, 10);
    if (isNaN(rateLimitNumber)) {
      return;
    }

    const currentRateLimit = this.rateLimitForIp$.value;
    if (rateLimitNumber === currentRateLimit) {
      this.notificationService.publish({
        message: 'Das Limit für Kontaktanfragen je IP-Adresse ist bereits auf diesen Wert eingestellt',
        type: 'warn',
      });
      return;
    }

    this.updatingRateLimitForIp$.next(true);

    const rateLimits = RateLimits.of({
      perMail: RateLimit.fullDay(this.rateLimitForMail$.value),
      perIp: RateLimit.fullDay(rateLimitNumber),
      overall: RateLimit.fullDay(this.rateLimitPerDay$.value),
    });

    this.inquiriesService
      .updateRateLimits(rateLimits)
      .pipe(
        first(),
        catchError((e) => {
          console.error('Failed to update rate limit for IP', e);
          this.notificationService.publish({
            message: 'Das Limit für Kontaktanfragen je IP-Adresse konnte nicht aktualisiert werden',
            type: 'error',
          });
          return EMPTY;
        }),
        finalize(() => {
          this.updatingRateLimitForIp$.next(false);
          this.notificationService.publish({
            type: 'success',
            message: `Das Limit für Kontaktanfragen je IP-Adresse wurde erfolgreich auf ${rateLimitNumber} aktualisiert`,
          });
        }),
        takeUntil(this.destroy$),
      )
      .subscribe(() => this.rateLimitForIp$.next(rateLimitNumber));
  }

  updatePendingRateLimitPerDay(rateLimit: string): void {
    const rateLimitNumber = parseInt(rateLimit, 10);
    if (isNaN(rateLimitNumber)) {
      return;
    }

    this.pendingRateLimitPerDay$.next(rateLimitNumber);
  }

  updateRateLimitPerDay(rateLimit: string): void {
    const rateLimitNumber = parseInt(rateLimit, 10);
    if (isNaN(rateLimitNumber)) {
      return;
    }

    const currentRateLimit = this.rateLimitPerDay$.value;
    if (rateLimitNumber === currentRateLimit) {
      this.notificationService.publish({
        message: 'Das Limit für Kontaktanfragen je Tag ist bereits auf diesen Wert eingestellt',
        type: 'warn',
      });
      return;
    }

    this.updatingRateLimitPerDay$.next(true);

    const rateLimits = RateLimits.of({
      perMail: RateLimit.fullDay(this.rateLimitForMail$.value),
      perIp: RateLimit.fullDay(this.rateLimitForIp$.value),
      overall: RateLimit.fullDay(rateLimitNumber),
    });

    this.inquiriesService
      .updateRateLimits(rateLimits)
      .pipe(
        first(),
        catchError((e) => {
          console.error('Failed to update rate limit per day', e);
          this.notificationService.publish({
            message: 'Das Limit für Kontaktanfragen je Tag konnte nicht aktualisiert werden',
            type: 'error',
          });
          return EMPTY;
        }),
        finalize(() => {
          this.updatingRateLimitPerDay$.next(false);
          this.notificationService.publish({
            type: 'success',
            message: `Das Limit für Kontaktanfragen je Tag wurde erfolgreich auf ${rateLimitNumber} aktualisiert`,
          });
        }),
        takeUntil(this.destroy$),
      )
      .subscribe(() => this.rateLimitPerDay$.next(rateLimitNumber));
  }

  toXLabels(_stats: Statistics[]): string[] {
    const maxDate = new Date(new Date().toDateString());

    const dates = Array.from({ length: 30 }, (_, i) => {
      return new Date(maxDate.getTime() - i * 24 * 60 * 60 * 1000);
    });

    dates.sort((a, b) => a.getTime() - b.getTime());

    return dates.map((d) => d.toLocaleDateString());
  }

  toYLabels(stats: Statistics[]): string[] {
    let maxRequests = Math.max(...stats.map((s) => s.totalRequests));
    if (maxRequests <= 0) {
      maxRequests = 3;
    }

    return Array.from({ length: maxRequests + 1 }, (_, i) => i.toString());
  }

  toData(stats: Statistics[]): number[] {
    const maxDate = new Date(new Date().toDateString());
    const data = Array.from({ length: 30 }, () => 0);

    for (const stat of stats) {
      const date = stat.dateRange.from;
      const daysAgo = Math.max(0, Math.floor((maxDate.getTime() - date.getTime()) / (24 * 60 * 60 * 1000)));
      data[data.length - 1 - daysAgo] = stat.totalRequests;
    }

    return data;
  }

  private reloadSettings(): void {
    this.loadingSettings$.next(true);

    this.inquiriesService
      .getSettings()
      .pipe(
        first(),
        catchError((e) => {
          console.error('Failed to load settings', e);
          this.notificationService.publish({
            message: 'Die Kontaktanfragen-Einstellungen konnten nicht geladen werden',
            type: 'error',
          });
          return EMPTY;
        }),
        finalize(() => {
          this.loadingSettings$.next(false);
          this.settingsLoaded$.next(true);
        }),
        takeUntil(this.destroy$),
      )
      .subscribe((settings) => {
        this.contactFormEnabled$.next(settings.enabled);

        const rateLimits = settings.rateLimits;
        this.rateLimitForMail$.next(rateLimits.perMail.maxRequests);
        this.pendingRateLimitForMail$.next(rateLimits.perMail.maxRequests);

        this.rateLimitForIp$.next(rateLimits.perIp.maxRequests);
        this.pendingRateLimitForIp$.next(rateLimits.perIp.maxRequests);

        this.rateLimitPerDay$.next(rateLimits.overall.maxRequests);
        this.pendingRateLimitPerDay$.next(rateLimits.overall.maxRequests);
      });
  }

  private reloadStats(): void {
    this.loadingStats$.next(true);

    this.inquiriesService
      .getStatistics()
      .pipe(
        first(),
        catchError((e) => {
          console.error('Failed to load stats', e);
          this.notificationService.publish({
            message: 'Die Kontaktanfragen-Statistiken konnten nicht geladen werden',
            type: 'error',
          });
          return EMPTY;
        }),
        finalize(() => {
          this.loadingStats$.next(false);
          this.statsLoaded$.next(true);
        }),
        takeUntil(this.destroy$),
      )
      .subscribe((stats) => this.stats$.next(stats));
  }

  toTotalInquiries(stats: Statistics[]): number {
    return stats.reduce((acc, s) => acc + s.totalRequests, 0);
  }
}
