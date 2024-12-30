import { validateProps } from '../../../../../util';
import { BotSettings } from './bot-settings';

export class Settings {
  readonly version: number;
  readonly botSettings: BotSettings;

  private constructor(props: { version: number; botSettings: BotSettings }) {
    validateProps(props);

    this.version = props.version;
    this.botSettings = props.botSettings;
  }

  static of(props: { version: number; botSettings: BotSettings }): Settings {
    return new Settings({
      version: props.version,
      botSettings: props.botSettings,
    });
  }

  updateBotApiToken(newToken: string, version: number): Settings {
    return new Settings({
      ...this,
      version,
      botSettings: this.botSettings.updateApiToken(newToken),
    });
  }

  clearBotApiToken(version: number): Settings {
    return new Settings({
      ...this,
      version,
      botSettings: this.botSettings.clearApiToken(),
    });
  }
}
