import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import {
  ActivatableChannel,
  Channel,
  ChannelType,
  EMAIL,
  MAIL,
  Notification,
  Origin,
  OriginType,
  Settings,
  SYSTEM,
  SystemSettings,
  TargetType,
  Telegram,
  TELEGRAM,
} from '../model';
import { Target } from '../model/target';
import { environment } from '../../../../../../environments';

interface QueryNotificationsResponse {
  total: number;
  notifications: NotificationDTO[];
}

interface NotificationDTO {
  id: string;
  version: number;
  origin: OriginDTO;
  target: TargetDTO;
  channels: ChannelDTO[];
  title: string;
  message: string;
  sentAt: string;
}

interface OriginDTO {
  type: OriginTypeDTO;
  id: string;
}

enum OriginTypeDTO {
  MAIL = 'MAIL',
}

interface TargetDTO {
  type: TargetTypeDTO;
  id: string;
}

enum TargetTypeDTO {
  SYSTEM = 'SYSTEM',
}

interface ChannelDTO {
  type: ChannelTypeDTO;
  mail?: string;
  telegram?: TelegramDTO;
}

enum ChannelTypeDTO {
  EMAIL = 'EMAIL',
  TELEGRAM = 'TELEGRAM',
}

interface TelegramDTO {
  chatId: string;
}

interface QuerySettingsResponse {
  settings: SettingsDTO;
}

interface SettingsDTO {
  version: number;
  systemSettings: SystemSettingsDTO;
}

interface SystemSettingsDTO {
  enabled: boolean;
  channels: ActivatableChannelDTO[];
}

interface ActivatableChannelDTO {
  active: boolean;
  channel: ChannelDTO;
}

interface EnableSystemNotificationsRequest {
  version: number;
}

interface EnableSystemNotificationsResponse {
  version: number;
}

interface DisableSystemNotificationsRequest {
  version: number;
}

interface DisableSystemNotificationsResponse {
  version: number;
}

interface UpdateSystemChannelRequest {
  version: number;
  channel: ChannelDTO;
}

interface UpdateSystemChannelResponse {
  version: number;
}

interface ActivateSystemChannelRequest {
  version: number;
  channelType: ChannelTypeDTO;
}

interface ActivateSystemChannelResponse {
  version: number;
}

interface DeactivateSystemChannelRequest {
  version: number;
  channelType: ChannelTypeDTO;
}

interface DeactivateSystemChannelResponse {
  version: number;
}

@Injectable()
export class NotificationsService {
  constructor(private readonly http: HttpClient) {}

  getNotifications(props: {
    from?: Date | null;
    to?: Date | null;
    skip?: number;
    limit?: number;
  }): Observable<{ total: number; notifications: Notification[] }> {
    let params = new HttpParams();
    if (props.from) {
      params = params.set('from', props.from.toISOString());
    }
    if (props.to) {
      params = params.set('to', props.to.toISOString());
    }
    if (props.skip) {
      params = params.set('skip', props.skip.toString());
    }
    if (props.limit) {
      params = params.set('limit', props.limit.toString());
    }

    return this.http
      .get<QueryNotificationsResponse>(`${environment.apiUrl}/notifications`, {
        params,
      })
      .pipe(
        map((response) => {
          return {
            total: response.total,
            notifications: response.notifications.map((notification) => {
              const id = notification.id;
              const version = notification.version;
              const origin = this.toInternalOrigin(notification.origin);
              const target = this.toInternalTarget(notification.target);
              const channels = this.toInternalChannels(notification.channels);
              const title = notification.title;
              const message = notification.message;
              const sentAt = new Date(notification.sentAt);

              return Notification.of({
                id,
                version,
                origin,
                target,
                channels,
                title,
                message,
                sentAt,
              });
            }),
          };
        }),
      );
  }

  getSettings(): Observable<Settings> {
    return this.http
      .get<QuerySettingsResponse>(`${environment.apiUrl}/notifications/settings`)
      .pipe(map((response) => this.toInternalSettings(response.settings)));
  }

  enableSystemNotifications(version: number): Observable<number> {
    const request: EnableSystemNotificationsRequest = { version };

    return this.http
      .post<EnableSystemNotificationsResponse>(`${environment.apiUrl}/notifications/settings/system/enable`, request)
      .pipe(map((response) => response.version));
  }

  disableSystemNotifications(version: number): Observable<number> {
    const request: DisableSystemNotificationsRequest = { version };

    return this.http
      .post<DisableSystemNotificationsResponse>(`${environment.apiUrl}/notifications/settings/system/disable`, request)
      .pipe(map((response) => response.version));
  }

  updateSystemChannel(version: number, channel: Channel): Observable<number> {
    const request: UpdateSystemChannelRequest = {
      version,
      channel: this.toApiChannel(channel),
    };

    return this.http
      .post<UpdateSystemChannelResponse>(`${environment.apiUrl}/notifications/settings/system/channels/update`, request)
      .pipe(map((response) => response.version));
  }

  activateSystemChannel(version: number, channelType: ChannelType): Observable<number> {
    const request: ActivateSystemChannelRequest = {
      version,
      channelType: this.toApiChannelType(channelType),
    };

    return this.http
      .post<ActivateSystemChannelResponse>(
        `${environment.apiUrl}/notifications/settings/system/channels/activate`,
        request,
      )
      .pipe(map((response) => response.version));
  }

  deactivateSystemChannel(version: number, channelType: ChannelType): Observable<number> {
    const request: DeactivateSystemChannelRequest = {
      version,
      channelType: this.toApiChannelType(channelType),
    };

    return this.http
      .post<DeactivateSystemChannelResponse>(
        `${environment.apiUrl}/notifications/settings/system/channels/deactivate`,
        request,
      )
      .pipe(map((response) => response.version));
  }

  private toInternalChannels(channels: ChannelDTO[]): Channel[] {
    return channels.map((channel) => this.toInternalChannel(channel));
  }

  private toInternalChannel(channel: ChannelDTO): Channel {
    const type = this.toInternalChannelType(channel.type);

    switch (type) {
      case EMAIL:
        return Channel.mail(channel.mail!);
      case TELEGRAM:
        return Channel.telegram(this.toInternalTelegram(channel.telegram!));
      default:
        throw new Error(`Unsupported channel type: ${type}`);
    }
  }

  private toApiChannel(channel: Channel): ChannelDTO {
    switch (channel.type) {
      case EMAIL:
        return { type: ChannelTypeDTO.EMAIL, mail: channel.mail.orElseThrow() };
      case TELEGRAM:
        return {
          type: ChannelTypeDTO.TELEGRAM,
          telegram: this.toApiTelegram(channel.telegram.orElseThrow()),
        };
      default:
        throw new Error(`Unsupported channel type: ${channel.type}`);
    }
  }

  private toInternalTelegram(telegram: TelegramDTO): Telegram {
    const chatId = telegram.chatId;

    return Telegram.of({ chatId });
  }

  private toApiTelegram(telegram: Telegram): TelegramDTO {
    const chatId = telegram.chatId;

    return { chatId };
  }

  private toInternalChannelType(type: ChannelTypeDTO): ChannelType {
    switch (type) {
      case ChannelTypeDTO.EMAIL:
        return EMAIL;
      case ChannelTypeDTO.TELEGRAM:
        return TELEGRAM;
      default:
        throw new Error(`Unsupported channel type: ${type}`);
    }
  }

  private toApiChannelType(type: ChannelType): ChannelTypeDTO {
    switch (type) {
      case EMAIL:
        return ChannelTypeDTO.EMAIL;
      case TELEGRAM:
        return ChannelTypeDTO.TELEGRAM;
      default:
        throw new Error(`Unsupported channel type: ${type}`);
    }
  }

  private toInternalTarget(target: TargetDTO): Target {
    const type = this.toInternalTargetType(target.type);
    const id = target.id;

    return Target.of({ type, id });
  }

  private toInternalTargetType(type: TargetTypeDTO): TargetType {
    switch (type) {
      case TargetTypeDTO.SYSTEM:
        return SYSTEM;
      default:
        throw new Error(`Unsupported target type: ${type}`);
    }
  }

  private toInternalOrigin(origin: OriginDTO): Origin {
    const type = this.toInternalOriginType(origin.type);
    const id = origin.id;

    return Origin.of({ type, id });
  }

  private toInternalOriginType(type: OriginTypeDTO): OriginType {
    switch (type) {
      case OriginTypeDTO.MAIL:
        return MAIL;
      default:
        throw new Error(`Unsupported origin type: ${type}`);
    }
  }

  private toInternalSettings(settings: SettingsDTO): Settings {
    const version = settings.version;
    const systemSettings = this.toInternalSystemSettings(settings.systemSettings);

    return Settings.of({ version, systemSettings });
  }

  private toInternalSystemSettings(systemSettings: SystemSettingsDTO): SystemSettings {
    const enabled = systemSettings.enabled;
    const channels = this.toInternalActivatableChannels(systemSettings.channels);

    return SystemSettings.of({ enabled, channels });
  }

  private toInternalActivatableChannels(channels: ActivatableChannelDTO[]): ActivatableChannel[] {
    return channels.map((channel) => this.toInternalActivatableChannel(channel));
  }

  private toInternalActivatableChannel(channel: ActivatableChannelDTO): ActivatableChannel {
    const active = channel.active;
    const c = this.toInternalChannel(channel.channel);

    return ActivatableChannel.of({ active, channel: c });
  }
}
