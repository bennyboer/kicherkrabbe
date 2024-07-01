export class Theme {
  readonly id: string;
  readonly name: string;

  private constructor(props: { id: string; name: string }) {
    this.id = props.id;
    this.name = props.name;
  }

  static of(props: { id: string; name: string }): Theme {
    return new Theme({
      id: props.id,
      name: props.name,
    });
  }
}
