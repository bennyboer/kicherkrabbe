import { validateProps } from '@kicherkrabbe/shared';
import { SystemSettings } from './system-settings';
import { Channel } from './channel';
import { ChannelType } from './channel-type';

export class Settings {
  readonly version: number;
  readonly systemSettings: SystemSettings;

  private constructor(props: { version: number; systemSettings: SystemSettings }) {
    validateProps(props);

    this.version = props.version;
    this.systemSettings = props.systemSettings;
  }

  static of(props: { version: number; systemSettings: SystemSettings }): Settings {
    return new Settings({
      version: props.version,
      systemSettings: props.systemSettings,
    });
  }

  enableSystemNotifications(version: number): Settings {
    return new Settings({
      ...this,
      version,
      systemSettings: this.systemSettings.enable(),
    });
  }

  disableSystemNotifications(version: number): Settings {
    return new Settings({
      ...this,
      version,
      systemSettings: this.systemSettings.disable(),
    });
  }

  updateSystemChannel(version: number, channel: Channel): Settings {
    return new Settings({
      ...this,
      version,
      systemSettings: this.systemSettings.updateChannel(channel),
    });
  }

  activateSystemChannel(version: number, channelType: ChannelType): Settings {
    return new Settings({
      ...this,
      version,
      systemSettings: this.systemSettings.activateChannel(channelType),
    });
  }

  deactivateSystemChannel(version: number, channelType: ChannelType): Settings {
    return new Settings({
      ...this,
      version,
      systemSettings: this.systemSettings.deactivateChannel(channelType),
    });
  }
}
