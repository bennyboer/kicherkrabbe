import { validateProps } from '../../../../../util';
import { someOrNone } from '@kicherkrabbe/shared';

export class Notes {
  readonly contains: string;
  readonly care: string;
  readonly safety: string;

  private constructor(props: { contains: string; care: string; safety: string }) {
    validateProps(props);

    this.contains = props.contains;
    this.care = props.care;
    this.safety = props.safety;
  }

  static of(props: { contains?: string; care?: string; safety?: string }): Notes {
    return new Notes({
      contains: someOrNone(props.contains).orElse(''),
      care: someOrNone(props.care).orElse(''),
      safety: someOrNone(props.safety).orElse(''),
    });
  }

  static empty(): Notes {
    return Notes.of({});
  }

  updateContains(note: string): Notes {
    return new Notes({
      ...this,
      contains: note,
    });
  }

  updateCare(note: string): Notes {
    return new Notes({
      ...this,
      care: note,
    });
  }

  updateSafety(note: string): Notes {
    return new Notes({
      ...this,
      safety: note,
    });
  }
}
