export interface ColorValue {
  red: number;
  green: number;
  blue: number;
}

export class Color {
  readonly id: string;
  readonly version: number;
  readonly name: string;
  readonly red: number;
  readonly green: number;
  readonly blue: number;
  readonly createdAt: Date;

  private constructor(props: {
    id: string;
    version: number;
    name: string;
    red: number;
    green: number;
    blue: number;
    createdAt: Date;
  }) {
    this.id = props.id;
    this.version = props.version;
    this.name = props.name;
    this.red = props.red;
    this.green = props.green;
    this.blue = props.blue;
    this.createdAt = props.createdAt;
  }

  static of(props: {
    id: string;
    version: number;
    name: string;
    red: number;
    green: number;
    blue: number;
    createdAt: Date;
  }): Color {
    return new Color({
      id: props.id,
      version: props.version,
      name: props.name,
      red: props.red,
      green: props.green,
      blue: props.blue,
      createdAt: props.createdAt,
    });
  }

  toValue(): ColorValue {
    return {
      red: this.red,
      green: this.green,
      blue: this.blue,
    };
  }
}
