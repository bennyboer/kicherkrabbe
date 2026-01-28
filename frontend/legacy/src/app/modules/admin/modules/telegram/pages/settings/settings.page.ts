import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, combineLatest, filter, finalize, first, map } from 'rxjs';
import { none, Option, some } from '../../../../../shared/modules/option';
import { Settings } from '../../model';
import { TelegramService } from '../../services';

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

  protected readonly updatingBotApiToken$ = new BehaviorSubject<boolean>(false);
  protected readonly clearingBotApiToken$ = new BehaviorSubject<boolean>(false);

  protected readonly pendingBotApiToken$ = new BehaviorSubject<string>('');
  protected readonly pendingBotApiTokenTouched$ = new BehaviorSubject<boolean>(false);
  protected readonly botApiTokenChanged$ = combineLatest([this.pendingBotApiToken$, this.settings$]).pipe(
    filter(([_, settings]) => settings.isSome()),
    map(
      ([pendingBotApiToken, settings]) => pendingBotApiToken !== settings.orElseThrow().botSettings.apiToken.orElse(''),
    ),
  );
  protected readonly botApiTokenUnchanged$ = this.botApiTokenChanged$.pipe(map((changed) => !changed));
  protected readonly botApiTokenValid$ = this.pendingBotApiToken$.pipe(map((token) => token.trim().length > 0));
  protected readonly botApiTokenInvalid$ = this.botApiTokenValid$.pipe(map((valid) => !valid));
  protected readonly botApiTokenInvalidAndTouched$ = combineLatest([
    this.pendingBotApiTokenTouched$,
    this.botApiTokenInvalid$,
  ]).pipe(map(([touched, invalid]) => touched && invalid));
  protected readonly canClearBotApiToken$ = this.settings$.pipe(
    map((settings) => settings.isSome() && settings.orElseThrow().botSettings.apiToken.isSome()),
  );
  protected readonly cannotClearBotApiToken$ = this.canClearBotApiToken$.pipe(map((canClear) => !canClear));

  protected readonly loading$ = combineLatest([
    this.loadingSettings$,
    this.updatingBotApiToken$,
    this.clearingBotApiToken$,
  ]).pipe(
    map(
      ([loadingSettings, updatingBotApiToken, clearingBotApiToken]) =>
        loadingSettings || updatingBotApiToken || clearingBotApiToken,
    ),
  );

  constructor(private readonly telegramService: TelegramService) {}

  ngOnInit(): void {
    this.reloadSettings();
  }

  ngOnDestroy(): void {
    this.settings$.complete();
    this.loadingSettings$.complete();
    this.settingsLoaded$.complete();
    this.updatingBotApiToken$.complete();
    this.clearingBotApiToken$.complete();
    this.pendingBotApiToken$.complete();
    this.pendingBotApiTokenTouched$.complete();
  }

  updatePendingBotApiToken(value: string): void {
    this.pendingBotApiToken$.next(value);

    if (!this.pendingBotApiTokenTouched$.value) {
      this.pendingBotApiTokenTouched$.next(true);
    }
  }

  updateBotApiToken(settings: Settings, newToken: string): void {
    if (this.updatingBotApiToken$.value) {
      return;
    }
    this.updatingBotApiToken$.next(true);

    this.telegramService
      .updateBotApiToken(settings.version, newToken)
      .pipe(
        first(),
        finalize(() => this.updatingBotApiToken$.next(false)),
      )
      .subscribe((version) => {
        const updatedSettings = this.settings$.value.orElseThrow().updateBotApiToken(newToken, version);
        this.settings$.next(some(updatedSettings));

        this.pendingBotApiToken$.next(this.settings$.value.orElseThrow().botSettings.apiToken.orElse(''));
      });
  }

  clearBotApiToken(settings: Settings): void {
    if (this.clearingBotApiToken$.value) {
      return;
    }
    this.clearingBotApiToken$.next(true);

    this.telegramService
      .clearBotApiToken(settings.version)
      .pipe(
        first(),
        finalize(() => this.clearingBotApiToken$.next(false)),
      )
      .subscribe((version) => {
        const updatedSettings = this.settings$.value.orElseThrow().clearBotApiToken(version);
        this.settings$.next(some(updatedSettings));

        this.pendingBotApiToken$.next('');
      });
  }

  private reloadSettings(): void {
    if (this.loadingSettings$.value) {
      return;
    }
    this.loadingSettings$.next(true);

    this.telegramService
      .getSettings()
      .pipe(
        first(),
        finalize(() => {
          this.loadingSettings$.next(false);
          this.settingsLoaded$.next(true);
        }),
      )
      .subscribe((settings) => {
        this.pendingBotApiToken$.next(settings.botSettings.apiToken.orElse(''));

        this.settings$.next(some(settings));
      });
  }
}
