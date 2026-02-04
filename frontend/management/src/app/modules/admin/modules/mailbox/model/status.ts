import { validateProps } from '@kicherkrabbe/shared';

export enum StatusType {
  READ = 'READ',
  UNREAD = 'UNREAD',
}

export class Status {
  readonly type: StatusType;
  readonly label: string;

  private constructor(props: { type: StatusType; label: string }) {
    validateProps(props);

    this.type = props.type;
    this.label = props.label;
  }

  static read(): Status {
    return new Status({
      type: StatusType.READ,
      label: 'Gelesen',
    });
  }

  static unread(): Status {
    return new Status({
      type: StatusType.UNREAD,
      label: 'Ungelesen',
    });
  }
}

export const READ = Status.read();
export const UNREAD = Status.unread();
