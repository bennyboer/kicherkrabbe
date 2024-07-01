import { Image } from '../../../../../util';
import { Availability } from './availability';

type ColorId = string;
type ThemeId = string;

export class Fabric {
  readonly id: string;
  readonly name: string;
  readonly image: Image;
  readonly colors: Set<ColorId>;
  readonly themes: Set<ThemeId>;
  readonly availability: Availability;

  private constructor(props: {
    id: string;
    name: string;
    image: Image;
    colors: Set<ColorId>;
    themes: Set<ThemeId>;
    availability: Availability;
  }) {
    this.id = props.id;
    this.name = props.name;
    this.image = props.image;
    this.colors = props.colors;
    this.themes = props.themes;
    this.availability = props.availability;
  }

  static of(props: {
    id: string;
    name: string;
    image: Image;
    colors: Set<ColorId>;
    themes: Set<ThemeId>;
    availability: Availability;
  }): Fabric {
    return new Fabric({
      id: props.id,
      name: props.name,
      image: props.image,
      colors: props.colors,
      themes: props.themes,
      availability: props.availability,
    });
  }

  isAvailableInAnyType(): boolean {
    return this.availability.isAvailableInAnyType();
  }

  getAvailableTypesLabel(): string {
    return this.availability.types
      .filter((typeAvailability) => typeAvailability.inStock)
      .map((typeAvailability) => typeAvailability.type.name)
      .join(', ');
  }

  getStockStatusLabel(): string {
    return this.availability.isAvailableInAnyType()
      ? 'Auf Lager'
      : 'Nicht auf Lager (nachbestellbar)';
  }
}
