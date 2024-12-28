import { validateProps } from '../../../../../util';

export enum InternalTargetType {
  SYSTEM = 'SYSTEM',
}

export class TargetType {
  readonly internal: InternalTargetType;

  private constructor(props: { internal: InternalTargetType }) {
    validateProps(props);

    this.internal = props.internal;
  }

  static system(): TargetType {
    return new TargetType({
      internal: InternalTargetType.SYSTEM,
    });
  }
}

export const SYSTEM = TargetType.system();
