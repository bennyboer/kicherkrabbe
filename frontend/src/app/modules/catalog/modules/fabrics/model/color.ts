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

export const BLUE = Color.of({
  id: 'blue',
  name: 'Blau',
  hex: '#008AB8',
});
export const BEIGE = Color.of({
  id: 'beige',
  name: 'Beige',
  hex: '#F5F5DC',
});
export const RED = Color.of({
  id: 'red',
  name: 'Rot',
  hex: '#FF3366',
});
export const ORANGE = Color.of({
  id: 'orange',
  name: 'Orange',
  hex: '#FFA500',
});
export const GREEN = Color.of({
  id: 'green',
  name: 'Grün',
  hex: '#B8F500',
});
export const WHITE = Color.of({
  id: 'white',
  name: 'Weiß',
  hex: '#FFFFFF',
});
export const PINK = Color.of({
  id: 'pink',
  name: 'Rosa',
  hex: '#FFC0CB',
});

export const COLORS = [BLUE, BEIGE, RED, GREEN, WHITE, ORANGE, PINK];
