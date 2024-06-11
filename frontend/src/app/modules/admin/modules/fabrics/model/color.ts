export type ColorId = string;

export class FabricColor {
  readonly id: ColorId;
  readonly name: string;
  readonly red: number;
  readonly green: number;
  readonly blue: number;

  private constructor(props: {
    id: ColorId;
    name: string;
    red: number;
    green: number;
    blue: number;
  }) {
    this.id = props.id;
    this.name = props.name;
    this.red = props.red;
    this.green = props.green;
    this.blue = props.blue;
  }

  static of(props: {
    id: ColorId;
    name: string;
    red: number;
    green: number;
    blue: number;
  }): FabricColor {
    return new FabricColor(props);
  }
}
