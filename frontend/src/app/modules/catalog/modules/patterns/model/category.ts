export class Category {
  readonly id: string;
  readonly name: string;

  private constructor(props: { id: string; name: string }) {
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

export const TOP: Category = Category.of({ id: 'top', name: 'Oberteil' });
export const PANTS: Category = Category.of({ id: 'pants', name: 'Hose' });
export const ONESIE: Category = Category.of({
  id: 'onesie',
  name: 'Einteiler',
});
export const DRESS: Category = Category.of({ id: 'dress', name: 'Kleid' });
export const ACCESSORY: Category = Category.of({
  id: 'accessory',
  name: 'Accessoire',
});

export const CATEGORIES: Category[] = [TOP, PANTS, ONESIE, DRESS, ACCESSORY];
