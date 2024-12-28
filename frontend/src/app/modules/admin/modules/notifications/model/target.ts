import { TargetType } from './target-type';
import { validateProps } from '../../../../../util';

export class Target {
  readonly type: TargetType;
  readonly id: string;

  private constructor(props: { id: string; type: TargetType }) {
    validateProps(props);

    this.id = props.id;
    this.type = props.type;
  }

  static of(props: { id: string; type: TargetType }): Target {
    return new Target({
      id: props.id,
      type: props.type,
    });
  }
}
