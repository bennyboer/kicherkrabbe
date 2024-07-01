export class Color {
  readonly id: string;
  readonly name: string;
  readonly hex: string;

  private constructor(props: { id: string; name: string; hex: string }) {
    this.id = props.id;
    this.name = props.name;
    this.hex = props.hex;
  }

  static of(props: { id: string; name: string; hex: string }): Color {
    return new Color({
      id: props.id,
      name: props.name,
      hex: props.hex,
    });
  }
}
