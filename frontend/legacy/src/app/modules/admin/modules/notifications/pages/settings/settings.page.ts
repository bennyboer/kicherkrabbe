import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, combineLatest, finalize, first, map, Observable } from 'rxjs';
import { NotificationsService } from '../../services';
import { DropdownComponent, DropdownItem, DropdownItemId, NotificationService } from '../../../../../shared';
import { none, Option, some, someOrNone } from '../../../../../shared/modules/option';
import { Channel, CHANNEL_TYPES, ChannelType, InternalChannelType, Settings } from '../../model';

@Component({
    selector: 'app-settings-page',
    templateUrl: './settings.page.html',
    styleUrls: ['./settings.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class SettingsPage implements OnInit, OnDestroy {
  protected readonly settings$ = new BehaviorSubject<Option<Settings>>(none());
  protected readonly loadingSettings$ = new BehaviorSubject<boolean>(false);
  protected readonly settingsLoaded$ = new BehaviorSubject<boolean>(false);
  protected readonly togglingSystemNotifications$ = new BehaviorSubject<boolean>(false);
  protected readonly updatingSystemChannel$ = new BehaviorSubject<boolean>(false);
  protected readonly activatingSystemChannel$ = new BehaviorSubject<boolean>(false);
  protected readonly deactivatingSystemChannel$ = new BehaviorSubject<boolean>(false);
  protected readonly pendingChannel$ = new BehaviorSubject<Option<Channel>>(none());

  protected readonly systemNotificationsEnabled$ = this.settings$.pipe(
    map((settings) => settings.map((s) => s.systemSettings.enabled).orElse(false)),
  );
  protected readonly activatableSystemChannels$ = this.settings$.pipe(
    map((settings) => settings.map((s) => s.systemSettings.channels).orElse([])),
  );
  protected readonly addChannelDropdownItems$: Observable<DropdownItem[]> = this.activatableSystemChannels$.pipe(
    map((activatableSystemChannels) => {
      const missingInternalChannelTypes = new Set<InternalChannelType>(CHANNEL_TYPES.map((type) => type.internal));
      for (const activatableChannel of activatableSystemChannels) {
        missingInternalChannelTypes.delete(activatableChannel.channel.type.internal);
      }

      const missingChannelTypes = Array.from(missingInternalChannelTypes).map((type) => {
        const actualType = CHANNEL_TYPES.find((t) => t.internal === type);
        if (!actualType) {
          throw new Error(`Unknown channel type: ${type}`);
        }
        return actualType;
      });

      return Array.from(missingChannelTypes).map((type) => ({
        id: type.internal,
        label: type.label,
      }));
    }),
  );
  protected readonly loading$ = combineLatest([
    this.loadingSettings$,
    this.togglingSystemNotifications$,
    this.updatingSystemChannel$,
    this.activatingSystemChannel$,
    this.deactivatingSystemChannel$,
  ]).pipe(
    map(
      ([
        loadingSettings,
        togglingSystemNotifications,
        updatingSystemChannel,
        activatingSystemChannel,
        deactivatingSystemChannel,
      ]) =>
        loadingSettings ||
        togglingSystemNotifications ||
        updatingSystemChannel ||
        activatingSystemChannel ||
        deactivatingSystemChannel,
    ),
  );

  constructor(
    private readonly notificationsService: NotificationsService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.reloadSettings();
  }

  ngOnDestroy(): void {
    this.settings$.complete();
    this.loadingSettings$.complete();
    this.settingsLoaded$.complete();
    this.togglingSystemNotifications$.complete();
    this.updatingSystemChannel$.complete();
    this.activatingSystemChannel$.complete();
    this.deactivatingSystemChannel$.complete();

    this.pendingChannel$.complete();
  }

  toggleSystemNotifications(settings: Settings): void {
    if (this.togglingSystemNotifications$.value) {
      return;
    }
    this.togglingSystemNotifications$.next(true);

    const isEnabled = settings.systemSettings.enabled;
    if (isEnabled) {
      this.disableSystemNotifications(settings);
    } else {
      this.enableSystemNotifications(settings);
    }
  }

  addChannelDropdownItemSelected(dropdown: DropdownComponent, itemIds: DropdownItemId[]): void {
    if (itemIds.length !== 1) {
      return;
    }

    const channelType = CHANNEL_TYPES.find((type) => type.internal === itemIds[0]);

    this.addPendingChannel(someOrNone(channelType).orElseThrow());

    dropdown.toggleOpened();
    dropdown.clearSelection();
  }

  updateSystemChannel(settings: Settings, channel: Channel): void {
    if (this.updatingSystemChannel$.value) {
      return;
    }
    this.updatingSystemChannel$.next(true);
    this.cancelPendingChannel();

    this.notificationsService
      .updateSystemChannel(settings.version, channel)
      .pipe(
        first(),
        finalize(() => this.updatingSystemChannel$.next(false)),
      )
      .subscribe({
        next: (version) => {
          const updatedSettings = settings.updateSystemChannel(version, channel);
          this.settings$.next(some(updatedSettings));

          this.notificationService.publish({
            message: 'System-Benachrichtigungs Kanal wurde aktualisiert.',
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Der System-Benachrichtigungs Kanal konnte nicht aktualisiert werden. Probieren Sie es erneut.',
            type: 'error',
          });
        },
      });
  }

  cancelPendingChannel(): void {
    this.pendingChannel$.next(none());
  }

  activateSystemChannel(settings: Settings, channelType: ChannelType): void {
    if (this.activatingSystemChannel$.value) {
      return;
    }
    this.activatingSystemChannel$.next(true);

    this.notificationsService
      .activateSystemChannel(settings.version, channelType)
      .pipe(
        first(),
        finalize(() => this.activatingSystemChannel$.next(false)),
      )
      .subscribe({
        next: (version) => {
          const updatedSettings = settings.activateSystemChannel(version, channelType);
          this.settings$.next(some(updatedSettings));

          this.notificationService.publish({
            message: 'System-Benachrichtigungs Kanal wurde aktiviert.',
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Der System-Benachrichtigungs Kanal konnte nicht aktiviert werden. Probieren Sie es erneut.',
            type: 'error',
          });
        },
      });
  }

  deactivateSystemChannel(settings: Settings, channelType: ChannelType): void {
    if (this.deactivatingSystemChannel$.value) {
      return;
    }
    this.deactivatingSystemChannel$.next(true);

    this.notificationsService
      .deactivateSystemChannel(settings.version, channelType)
      .pipe(
        first(),
        finalize(() => this.deactivatingSystemChannel$.next(false)),
      )
      .subscribe({
        next: (version) => {
          const updatedSettings = settings.deactivateSystemChannel(version, channelType);
          this.settings$.next(some(updatedSettings));

          this.notificationService.publish({
            message: 'System-Benachrichtigungs Kanal wurde deaktiviert.',
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Der System-Benachrichtigungs Kanal konnte nicht deaktiviert werden. Probieren Sie es erneut.',
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

    this.notificationsService
      .getSettings()
      .pipe(
        first(),
        finalize(() => {
          this.loadingSettings$.next(false);
          this.settingsLoaded$.next(true);
        }),
      )
      .subscribe((settings) => this.settings$.next(some(settings)));
  }

  private disableSystemNotifications(settings: Settings): void {
    this.notificationsService
      .disableSystemNotifications(settings.version)
      .pipe(
        first(),
        finalize(() => this.togglingSystemNotifications$.next(false)),
      )
      .subscribe({
        next: (version) => {
          const updatedSettings = settings.disableSystemNotifications(version);
          this.settings$.next(some(updatedSettings));

          this.notificationService.publish({
            message: 'System-Benachrichtigungen wurden deaktiviert.',
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Die System-Benachrichtigungen konnten nicht deaktiviert werden. Probieren Sie es erneut.',
            type: 'error',
          });
        },
      });
  }

  private enableSystemNotifications(settings: Settings): void {
    this.notificationsService
      .enableSystemNotifications(settings.version)
      .pipe(
        first(),
        finalize(() => this.togglingSystemNotifications$.next(false)),
      )
      .subscribe({
        next: (version) => {
          const updatedSettings = settings.enableSystemNotifications(version);
          this.settings$.next(some(updatedSettings));

          this.notificationService.publish({
            message: 'System-Benachrichtigungen wurden aktiviert.',
            type: 'success',
          });
        },
        error: () => {
          this.notificationService.publish({
            message: 'Die System-Benachrichtigungen konnten nicht aktiviert werden. Probieren Sie es erneut.',
            type: 'error',
          });
        },
      });
  }

  private addPendingChannel(channelType: ChannelType): void {
    switch (channelType.internal) {
      case InternalChannelType.EMAIL:
        this.pendingChannel$.next(some(Channel.mail('')));
        break;
      case InternalChannelType.TELEGRAM:
        this.pendingChannel$.next(some(Channel.telegram({ chatId: '' })));
        break;
      default:
        throw new Error(`Unknown channel type: ${channelType.internal}`);
    }
  }
}
