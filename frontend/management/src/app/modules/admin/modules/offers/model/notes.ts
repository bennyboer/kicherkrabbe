import {Option, someOrNone, validateProps} from '@kicherkrabbe/shared';

export class Notes {
  readonly description: string;
  readonly contains: Option<string>;
  readonly care: Option<string>;
  readonly safety: Option<string>;

  private constructor(props: {
    description: string;
    contains: Option<string>;
    care: Option<string>;
    safety: Option<string>
  }) {
    validateProps(props);

    this.description = props.description;
    this.contains = props.contains;
    this.care = props.care;
    this.safety = props.safety;
  }

  static of(props: {
    description?: string;
    contains?: string | null | undefined;
    care?: string | null | undefined;
    safety?: string | null | undefined
  }): Notes {
    return new Notes({
      description: someOrNone(props.description).orElse(''),
      contains: someOrNone(props.contains),
      care: someOrNone(props.care),
      safety: someOrNone(props.safety),
    });
  }

  static empty(): Notes {
    return Notes.of({});
  }

  updateDescription(note: string): Notes {
    return new Notes({
      ...this,
      description: note,
    });
  }

  updateContains(note: string): Notes {
    return new Notes({
      ...this,
      contains: someOrNone(note).filter(n => n.trim().length > 0),
    });
  }

  updateCare(note: string): Notes {
    return new Notes({
      ...this,
      care: someOrNone(note).filter(n => n.trim().length > 0),
    });
  }

  updateSafety(note: string): Notes {
    return new Notes({
      ...this,
      safety: someOrNone(note).filter(n => n.trim().length > 0),
    });
  }
}
