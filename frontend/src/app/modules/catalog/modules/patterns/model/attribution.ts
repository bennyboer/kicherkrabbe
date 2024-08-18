import { Option, someOrNone, validateProps } from '../../../../../util';

export class PatternAttribution {
  readonly originalPatternName: Option<string>;
  readonly designer: Option<string>;

  private constructor(props: {
    originalPatternName: Option<string>;
    designer: Option<string>;
  }) {
    validateProps(props);

    this.originalPatternName = props.originalPatternName;
    this.designer = props.designer;
  }

  static of(props: {
    originalPatternName?: string;
    designer?: string;
  }): PatternAttribution {
    return new PatternAttribution({
      originalPatternName: someOrNone(props.originalPatternName),
      designer: someOrNone(props.designer),
    });
  }

  hasSome(): boolean {
    return this.originalPatternName.isSome() || this.designer.isSome();
  }
}
