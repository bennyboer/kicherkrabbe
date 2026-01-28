import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, combineLatest, filter, finalize, first, map } from 'rxjs';
import { none, Option, some } from '../../../../../shared/modules/option';
import { MailingService } from '../../services';
import { Settings } from '../../model';
import { NotificationService } from '../../../../../shared';

@Component({
  selector: 'app-settings-page',
  templateUrl: './settings.page.html',
  styleUrls: ['./settings.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class SettingsPage implements OnInit, OnDestroy {
  protected readonly settings$ = new BehaviorSubject<Option<Settings>>(none());
  protected readonly loadingSettings$ = new BehaviorSubject<boolean>(false);
  protected readonly settingsLoaded$ = new BehaviorSubject<boolean>(false);

  protected readonly pendingRateLimit$ = new BehaviorSubject<string>('0');
  protected readonly pendingRateLimitTouched$ = new BehaviorSubject<boolean>(false);
  protected readonly updatingRateLimit$ = new BehaviorSubject<boolean>(false);
  protected readonly pendingRateLimitAsString$ = this.pendingRateLimit$.pipe(map((limit) => limit.toString()));
  protected readonly rateLimitUnchanged$ = combineLatest([this.pendingRateLimit$, this.settings$]).pipe(
    filter(([_, settings]) => settings.isSome()),
    map(([pendingRateLimit, settings]) => pendingRateLimit === settings.orElseThrow().rateLimit.limit.toString()),
  );
  protected readonly rateLimitValid$ = this.pendingRateLimit$.pipe(
    map((limit) => {
      let result = 0;
      try {
        result = parseInt(limit, 10);
      } catch (e) {
        result = 0;
      }

      if (result.toString() !== limit) {
        return false;
      }

      return result >= 0;
    }),
  );
  protected readonly rateLimitInvalid$ = this.rateLimitValid$.pipe(map((valid) => !valid));
  protected readonly rateLimitInvalidAndTouched$ = combineLatest([
    this.pendingRateLimitTouched$,
    this.rateLimitInvalid$,
  ]).pipe(map(([touched, invalid]) => touched && invalid));

  protected readonly updatingMailgunApiToken$ = new BehaviorSubject<boolean>(false);
  protected readonly clearingMailgunApiToken$ = new BehaviorSubject<boolean>(false);

  protected readonly pendingMailgunApiToken$ = new BehaviorSubject<string>('');
  protected readonly pendingMailgunApiTokenTouched$ = new BehaviorSubject<boolean>(false);
  protected readonly mailgunApiTokenChanged$ = combineLatest([this.pendingMailgunApiToken$, this.settings$]).pipe(
    filter(([_, settings]) => settings.isSome()),
    map(
      ([pendingMailgunApiToken, settings]) =>
        pendingMailgunApiToken !== settings.orElseThrow().mailgun.apiToken.orElse(''),
    ),
  );
  protected readonly mailgunApiTokenUnchanged$ = this.mailgunApiTokenChanged$.pipe(map((changed) => !changed));
  protected readonly mailgunApiTokenValid$ = this.pendingMailgunApiToken$.pipe(map((token) => token.trim().length > 0));
  protected readonly mailgunApiTokenInvalid$ = this.mailgunApiTokenValid$.pipe(map((valid) => !valid));
  protected readonly mailgunApiTokenInvalidAndTouched$ = combineLatest([
    this.pendingMailgunApiTokenTouched$,
    this.mailgunApiTokenInvalid$,
  ]).pipe(map(([touched, invalid]) => touched && invalid));
  protected readonly canClearMailgunApiToken$ = this.settings$.pipe(
    map((settings) => settings.isSome() && settings.orElseThrow().mailgun.apiToken.isSome()),
  );
  protected readonly cannotClearMailgunApiToken$ = this.canClearMailgunApiToken$.pipe(map((canClear) => !canClear));

  protected readonly loading$ = combineLatest([
    this.loadingSettings$,
    this.updatingMailgunApiToken$,
    this.clearingMailgunApiToken$,
    this.updatingRateLimit$,
  ]).pipe(
    map(
      ([loadingSettings, updatingMailgunApiToken, clearingMailgunApiToken, updatingRateLimit]) =>
        loadingSettings || updatingMailgunApiToken || clearingMailgunApiToken || updatingRateLimit,
    ),
  );

  constructor(
    private readonly mailingService: MailingService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.reloadSettings();
  }

  ngOnDestroy(): void {
    this.settings$.complete();
    this.loadingSettings$.complete();
    this.settingsLoaded$.complete();
    this.updatingMailgunApiToken$.complete();
    this.clearingMailgunApiToken$.complete();
    this.pendingMailgunApiToken$.complete();
    this.pendingMailgunApiTokenTouched$.complete();
    this.pendingRateLimit$.complete();
    this.updatingRateLimit$.complete();
    this.pendingRateLimitTouched$.complete();
  }

  updateRateLimit(settings: Settings, newLimitStr: string): void {
    if (this.updatingRateLimit$.value) {
      return;
    }
    this.updatingRateLimit$.next(true);

    const newLimit = parseInt(newLimitStr, 10);
    const newDurationInMs = 24 * 60 * 60 * 1000;

    this.mailingService
      .updateRateLimit(settings.version, newDurationInMs, newLimit)
      .pipe(
        first(),
        finalize(() => this.updatingRateLimit$.next(false)),
      )
      .subscribe({
        next: (version) => {
          const updatedSettings = this.settings$.value
            .orElseThrow()
            .updateRateLimit(version, newDurationInMs, newLimit);
          this.settings$.next(some(updatedSettings));

          this.pendingRateLimit$.next(this.settings$.value.orElseThrow().rateLimit.limit.toString());

          this.notificationService.publish({
            message: 'Das Limit wurde erfolgreich aktualisiert.',
            type: 'success',
          });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            message: 'Das Limit konnte nicht aktualisiert werden. Bitte versuche es erneut.',
            type: 'error',
          });
        },
      });
  }

  updatePendingRateLimit(value: string): void {
    this.pendingRateLimit$.next(value);

    if (!this.pendingRateLimitTouched$.value) {
      this.pendingRateLimitTouched$.next(true);
    }
  }

  updatePendingMailgunApiToken(value: string): void {
    this.pendingMailgunApiToken$.next(value);

    if (!this.pendingMailgunApiTokenTouched$.value) {
      this.pendingMailgunApiTokenTouched$.next(true);
    }
  }

  updateMailgunApiToken(settings: Settings, newToken: string): void {
    if (this.updatingMailgunApiToken$.value) {
      return;
    }
    this.updatingMailgunApiToken$.next(true);

    this.mailingService
      .updateMailgunApiToken(settings.version, newToken)
      .pipe(
        first(),
        finalize(() => this.updatingMailgunApiToken$.next(false)),
      )
      .subscribe({
        next: (version) => {
          const updatedSettings = this.settings$.value.orElseThrow().updateMailgunApiToken(version, newToken);
          this.settings$.next(some(updatedSettings));

          this.pendingMailgunApiToken$.next(this.settings$.value.orElseThrow().mailgun.apiToken.orElse(''));

          this.notificationService.publish({
            message: 'Der Mailgun API-Token wurde erfolgreich aktualisiert.',
            type: 'success',
          });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            message: 'Der Mailgun API-Token konnte nicht aktualisiert werden. Bitte versuche es erneut.',
            type: 'error',
          });
        },
      });
  }

  clearMailgunApiToken(settings: Settings): void {
    if (this.clearingMailgunApiToken$.value) {
      return;
    }
    this.clearingMailgunApiToken$.next(true);

    this.mailingService
      .clearMailgunApiToken(settings.version)
      .pipe(
        first(),
        finalize(() => this.clearingMailgunApiToken$.next(false)),
      )
      .subscribe({
        next: (version) => {
          const updatedSettings = this.settings$.value.orElseThrow().clearMailgunApiToken(version);
          this.settings$.next(some(updatedSettings));

          this.pendingMailgunApiToken$.next('');

          this.notificationService.publish({
            message: 'Der Mailgun API-Token wurde erfolgreich entfernt.',
            type: 'success',
          });
        },
        error: (e) => {
          console.error(e);
          this.notificationService.publish({
            message: 'Der Mailgun API-Token konnte nicht entfernt werden. Bitte versuche es erneut.',
            type: 'error',
          });
        },
      });
  }

  private reloadSettings(): void {
    if (this.loadingSettings$.value) {
      return;
    }
    this.loadingSettings$.next(true);

    this.mailingService
      .getSettings()
      .pipe(
        first(),
        finalize(() => {
          this.loadingSettings$.next(false);
          this.settingsLoaded$.next(true);
        }),
      )
      .subscribe((settings) => {
        this.pendingRateLimit$.next(settings.rateLimit.limit.toString());
        this.pendingMailgunApiToken$.next(settings.mailgun.apiToken.orElse(''));

        this.settings$.next(some(settings));
      });
  }
}
