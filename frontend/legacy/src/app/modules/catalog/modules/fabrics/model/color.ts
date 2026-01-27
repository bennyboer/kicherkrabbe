export class Color {
  readonly id: string;
  readonly name: string;
  readonly red: number;
  readonly green: number;
  readonly blue: number;

  private constructor(props: { id: string; name: string; red: number; green: number; blue: number }) {
    this.id = props.id;
    this.name = props.name;
    this.red = props.red;
    this.green = props.green;
    this.blue = props.blue;
  }

  static of(props: { id: string; name: string; red: number; green: number; blue: number }): Color {
    return new Color({
      id: props.id,
      name: props.name,
      red: props.red,
      green: props.green,
      blue: props.blue,
    });
  }
}
