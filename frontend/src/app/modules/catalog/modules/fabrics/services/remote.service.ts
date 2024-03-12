import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import {
  ABSTRACT,
  ANIMALS,
  Availability,
  BEIGE,
  BLUE,
  Color,
  Fabric,
  FANTASY,
  FLORAL,
  FRENCH_TERRY,
  GREEN,
  JERSEY,
  MARITIM,
  ORANGE,
  PINK,
  RED,
  SPACE,
  Theme,
  TypeAvailability,
  WHITE,
} from '../model';

@Injectable()
export class RemoteFabricsService {
  private readonly mockFabrics: Fabric[] = [
    Fabric.of({
      id: 'baby-orca',
      name: 'Baby Orca',
      image: {
        url: '/assets/images/fabrics/baby_orca.jpg',
      },
      colors: new Set<Color>([BLUE]),
      themes: new Set<Theme>([ANIMALS, MARITIM]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'blumenwal',
      name: 'Blumenwal',
      image: {
        url: '/assets/images/fabrics/blumenwal.jpg',
      },
      colors: new Set<Color>([BEIGE]),
      themes: new Set<Theme>([FLORAL, ANIMALS, MARITIM]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'blumenwiese',
      name: 'Blumenwiese',
      image: {
        url: '/assets/images/fabrics/blumenwiese.jpg',
      },
      colors: new Set<Color>([RED, GREEN, WHITE]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'blaetterkuh',
      name: 'Blätterkuh',
      image: {
        url: '/assets/images/fabrics/blätterkuh.jpg',
      },
      colors: new Set<Color>([BLUE]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'dschungelfreunde',
      name: 'Dschungelfreunde',
      image: {
        url: '/assets/images/fabrics/dschungelfreunde.jpg',
      },
      colors: new Set<Color>([BLUE]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'elch',
      name: 'Elch',
      image: {
        url: '/assets/images/fabrics/elch.jpg',
      },
      colors: new Set<Color>([BLUE]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'eule-gruen',
      name: 'Eule Grün',
      image: {
        url: '/assets/images/fabrics/eule_grün.jpg',
      },
      colors: new Set<Color>([GREEN]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'floral-woodland',
      name: 'Floral Woodland',
      image: {
        url: '/assets/images/fabrics/floral_woodland.jpg',
      },
      colors: new Set<Color>([WHITE]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'frosch-und-libelle',
      name: 'Frosch und Libelle',
      image: {
        url: '/assets/images/fabrics/frosch_und_libelle.jpg',
      },
      colors: new Set<Color>([BLUE, GREEN]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'fuchsmama-und-baby',
      name: 'Fuchsmama und Baby',
      image: {
        url: '/assets/images/fabrics/fuchsmama_und_baby.jpg',
      },
      colors: new Set<Color>([ORANGE]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'himmelsfreunde',
      name: 'Himmelsfreunde',
      image: {
        url: '/assets/images/fabrics/himmelsfreunde.jpg',
      },
      colors: new Set<Color>([BLUE]),
      themes: new Set<Theme>([FANTASY]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'koalas',
      name: 'Koalas',
      image: {
        url: '/assets/images/fabrics/koalas.jpg',
      },
      colors: new Set<Color>([PINK]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'leoprint',
      name: 'Leoprint',
      image: {
        url: '/assets/images/fabrics/leoprint.jpg',
      },
      colors: new Set<Color>([PINK, RED]),
      themes: new Set<Theme>([ANIMALS, ABSTRACT]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'nachthimmel',
      name: 'Nachthimmel',
      image: {
        url: '/assets/images/fabrics/nachthimmel.jpg',
      },
      colors: new Set<Color>([BLUE, ORANGE]),
      themes: new Set<Theme>([ANIMALS, FANTASY]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'osterblumen',
      name: 'Osterblumen',
      image: {
        url: '/assets/images/fabrics/osterblumen.jpg',
      },
      colors: new Set<Color>([WHITE, RED]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'red-panda',
      name: 'Red Panda',
      image: {
        url: '/assets/images/fabrics/red_panda.jpg',
      },
      colors: new Set<Color>([GREEN]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'regenbogen',
      name: 'Regenbogen',
      image: {
        url: '/assets/images/fabrics/regenbogen.jpg',
      },
      colors: new Set<Color>([WHITE, RED, PINK]),
      themes: new Set<Theme>([FANTASY]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'robbenspaß',
      name: 'Robbenspaß',
      image: {
        url: '/assets/images/fabrics/robbenspaß.jpg',
      },
      colors: new Set<Color>([BLUE]),
      themes: new Set<Theme>([ANIMALS, MARITIM]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'rosa-quallen',
      name: 'Rosa Quallen',
      image: {
        url: '/assets/images/fabrics/rosa_quallen.jpg',
      },
      colors: new Set<Color>([PINK]),
      themes: new Set<Theme>([ANIMALS, MARITIM]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'rosa-blumen',
      name: 'Rosa Blumen',
      image: {
        url: '/assets/images/fabrics/rosa_blumen.jpg',
      },
      colors: new Set<Color>([PINK]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'schlafende-penny',
      name: 'Schlafende Penny',
      image: {
        url: '/assets/images/fabrics/schlafende_penny.jpg',
      },
      colors: new Set<Color>([BLUE]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'space-baer',
      name: 'Space Bär',
      image: {
        url: '/assets/images/fabrics/space_bär.jpg',
      },
      colors: new Set<Color>([WHITE]),
      themes: new Set<Theme>([ANIMALS, SPACE]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'streifenwal',
      name: 'Streifenwal',
      image: {
        url: '/assets/images/fabrics/streifenwal.jpg',
      },
      colors: new Set<Color>([WHITE, BLUE]),
      themes: new Set<Theme>([ANIMALS, MARITIM]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'sweet-rabbit',
      name: 'Sweet Rabbit',
      image: {
        url: '/assets/images/fabrics/sweet_rabbit.jpg',
      },
      colors: new Set<Color>([PINK]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'tukanbaby',
      name: 'Tukanbaby',
      image: {
        url: '/assets/images/fabrics/tukanbaby.jpg',
      },
      colors: new Set<Color>([BLUE]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'uboot',
      name: 'U-Boot',
      image: {
        url: '/assets/images/fabrics/uboot.jpg',
      },
      colors: new Set<Color>([GREEN]),
      themes: new Set<Theme>([ANIMALS, MARITIM]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'waldfreunde',
      name: 'Waldfreunde',
      image: {
        url: '/assets/images/fabrics/waldfreunde.jpg',
      },
      colors: new Set<Color>([WHITE]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'wanda-aqua',
      name: 'Wanda Aqua',
      image: {
        url: '/assets/images/fabrics/wanda_aqua.jpg',
      },
      colors: new Set<Color>([BLUE]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'zebra-blush',
      name: 'Zebra Blush',
      image: {
        url: '/assets/images/fabrics/zebra_blush.jpg',
      },
      colors: new Set<Color>([PINK]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
  ];

  getFabrics(): Observable<Fabric[]> {
    return of(this.mockFabrics);
  }
}
