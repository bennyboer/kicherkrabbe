import { ActivatableChannel } from './activatable-channel';
import { someOrNone, validateProps } from '@kicherkrabbe/shared';
import { Channel } from './channel';
import { ChannelType } from './channel-type';

export class SystemSettings {
  readonly enabled: boolean;
  readonly channels: ActivatableChannel[];

  private constructor(props: { enabled: boolean; channels: ActivatableChannel[] }) {
    validateProps(props);

    this.enabled = props.enabled;
    this.channels = props.channels;
  }

  static of(props: { enabled?: boolean; channels: ActivatableChannel[] }): SystemSettings {
    return new SystemSettings({
      enabled: someOrNone(props.enabled).orElse(false),
      channels: props.channels,
    });
  }

  enable(): SystemSettings {
    return new SystemSettings({
      ...this,
      enabled: true,
    });
  }

  disable(): SystemSettings {
    return new SystemSettings({
      ...this,
      enabled: false,
    });
  }

  updateChannel(channel: Channel): SystemSettings {
    const isAlreadyPresent = this.channels.some(
      (activatableChannel) => activatableChannel.channel.type === channel.type,
    );
    let updatedChannels: ActivatableChannel[] = [];
    if (isAlreadyPresent) {
      updatedChannels = this.channels.map((activatableChannel) =>
        activatableChannel.channel.type === channel.type
          ? ActivatableChannel.of({
              active: activatableChannel.active,
              channel,
            })
          : activatableChannel,
      );
    } else {
      updatedChannels = [...this.channels, ActivatableChannel.of({ active: false, channel })];
    }

    return new SystemSettings({
      ...this,
      channels: updatedChannels,
    });
  }

  activateChannel(channelType: ChannelType): SystemSettings {
    const updatedChannels = this.channels.map((activatableChannel) =>
      activatableChannel.channel.type === channelType
        ? ActivatableChannel.of({
            active: true,
            channel: activatableChannel.channel,
          })
        : activatableChannel,
    );

    return new SystemSettings({
      ...this,
      channels: updatedChannels,
    });
  }

  deactivateChannel(channelType: ChannelType): SystemSettings {
    const updatedChannels = this.channels.map((activatableChannel) =>
      activatableChannel.channel.type === channelType
        ? ActivatableChannel.of({
            active: false,
            channel: activatableChannel.channel,
          })
        : activatableChannel,
    );

    return new SystemSettings({
      ...this,
      channels: updatedChannels,
    });
  }
}
