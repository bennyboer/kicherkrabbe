export class Type {
  readonly id: string;
  readonly name: string;

  private constructor(props: { id: string; name: string }) {
    this.id = props.id;
    this.name = props.name;
  }

  static of(props: { id: string; name: string }): Type {
    return new Type({
      id: props.id,
      name: props.name,
    });
  }
}

export const FRENCH_TERRY = Type.of({
  id: 'french-terry',
  name: 'Sommersweat / French Terry',
});
export const JERSEY = Type.of({ id: 'jersey', name: 'Jersey' });

export const TYPES = [FRENCH_TERRY, JERSEY];
