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

export const FLORAL = Theme.of({ id: 'floral', name: 'Floral' });
export const ANIMALS = Theme.of({ id: 'animals', name: 'Tiere' });
export const MARITIM = Theme.of({ id: 'maritim', name: 'Maritim' });
export const FANTASY = Theme.of({ id: 'fantasie', name: 'Fantasie' });
export const ABSTRACT = Theme.of({ id: 'abstrakt', name: 'Abstrakt' });
export const SPACE = Theme.of({ id: 'space', name: 'Weltraum' });
export const FRUITS = Theme.of({ id: 'fruits', name: 'Früchte' });
export const SUMMER = Theme.of({ id: 'summer', name: 'Sommer' });

export const THEMES = [
  FLORAL,
  ANIMALS,
  MARITIM,
  FANTASY,
  ABSTRACT,
  SPACE,
  FRUITS,
  SUMMER,
];
