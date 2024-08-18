import { validateProps } from '../../../../../util';

export class Category {
  readonly id: string;
  readonly name: string;

  private constructor(props: { id: string; name: string }) {
    validateProps(props);

    this.id = props.id;
    this.name = props.name;
  }

  static of(props: { id: string; name: string }): Category {
    return new Category({
      id: props.id,
      name: props.name,
    });
  }
}
