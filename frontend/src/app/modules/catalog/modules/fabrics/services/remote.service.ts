import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import {
  ABSTRACT,
  ANIMALS,
  Availability,
  BEIGE,
  BLACK,
  BLUE,
  BROWN,
  Color,
  Fabric,
  FALL,
  FANTASY,
  FLORAL,
  FRENCH_TERRY,
  FRUITS,
  GOLD,
  GREEN,
  GREY,
  HALLOWEEN,
  JERSEY,
  MARITIM,
  ORANGE,
  PINK,
  RED,
  SPACE,
  SUMMER,
  SWEETS,
  Theme,
  TypeAvailability,
  WHITE,
  WINTER,
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
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: true }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'blaetterkuh',
      name: 'Blätterkuh',
      image: {
        url: '/assets/images/fabrics/blaetterkuh.jpg',
      },
      colors: new Set<Color>([BLUE]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
        url: '/assets/images/fabrics/eule_gruen.jpg',
      },
      colors: new Set<Color>([GREEN]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: true }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: true }),
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
          TypeAvailability.of({ type: JERSEY, inStock: true }),
        ],
      }),
    }),
    Fabric.of({
      id: 'osterbluemchen',
      name: 'Osterblümchen',
      image: {
        url: '/assets/images/fabrics/osterbluemchen.jpg',
      },
      colors: new Set<Color>([WHITE, RED]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: true }),
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
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: true }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
        ],
      }),
    }),
    Fabric.of({
      id: 'robbenspaß',
      name: 'Robbenspaß',
      image: {
        url: '/assets/images/fabrics/robbenspass.jpg',
      },
      colors: new Set<Color>([BLUE]),
      themes: new Set<Theme>([ANIMALS, MARITIM]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
        url: '/assets/images/fabrics/space_baer.jpg',
      },
      colors: new Set<Color>([WHITE]),
      themes: new Set<Theme>([ANIMALS, SPACE]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: true }),
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
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: true }),
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
          TypeAvailability.of({ type: JERSEY, inStock: true }),
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
          TypeAvailability.of({ type: JERSEY, inStock: true }),
        ],
      }),
    }),
    Fabric.of({
      id: 'blumen-1',
      name: 'Blumen 1',
      image: {
        url: '/assets/images/fabrics/blumen_1.jpg',
      },
      colors: new Set<Color>([PINK, WHITE]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'blumen-2',
      name: 'Blumen 2',
      image: {
        url: '/assets/images/fabrics/blumen_2.jpg',
      },
      colors: new Set<Color>([PINK, WHITE]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
        ],
      }),
    }),
    Fabric.of({
      id: 'blumen-3',
      name: 'Blumen 3',
      image: {
        url: '/assets/images/fabrics/blumen_3.jpg',
      },
      colors: new Set<Color>([GREY, BLUE]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'blumen-4',
      name: 'Blumen 4',
      image: {
        url: '/assets/images/fabrics/blumen_4.jpg',
      },
      colors: new Set<Color>([PINK, WHITE]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
        ],
      }),
    }),
    Fabric.of({
      id: 'bluemchen',
      name: 'Blümchen',
      image: {
        url: '/assets/images/fabrics/bluemchen.jpg',
      },
      colors: new Set<Color>([PINK, WHITE]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'blumen-neutral',
      name: 'Blumen Neutral',
      image: {
        url: '/assets/images/fabrics/blumen_neutral.jpg',
      },
      colors: new Set<Color>([PINK, BEIGE]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
        ],
      }),
    }),
    Fabric.of({
      id: 'burgundy-biene',
      name: 'Burgundy Biene',
      image: {
        url: '/assets/images/fabrics/burgundy_biene.jpg',
      },
      colors: new Set<Color>([RED]),
      themes: new Set<Theme>([FLORAL, ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'dots',
      name: 'Dots',
      image: {
        url: '/assets/images/fabrics/dots.jpg',
      },
      colors: new Set<Color>([WHITE]),
      themes: new Set<Theme>([FLORAL, ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
        ],
      }),
    }),
    Fabric.of({
      id: 'erdbeer-cocktail',
      name: 'Erdbeer Cocktail',
      image: {
        url: '/assets/images/fabrics/erdbeer_cocktail.jpg',
      },
      colors: new Set<Color>([PINK]),
      themes: new Set<Theme>([FRUITS, SUMMER]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'flamingo',
      name: 'Flamingo',
      image: {
        url: '/assets/images/fabrics/flamingo.jpg',
      },
      colors: new Set<Color>([PINK]),
      themes: new Set<Theme>([ANIMALS, FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: true }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'floral-motte',
      name: 'Floral Motte',
      image: {
        url: '/assets/images/fabrics/floral_motte.jpg',
      },
      colors: new Set<Color>([WHITE, BROWN]),
      themes: new Set<Theme>([ANIMALS, FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: true }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'kicherkrabbes-lieblingsblume',
      name: 'Kicherkrabbes Lieblingsblume',
      image: {
        url: '/assets/images/fabrics/kicherkrabbes_lieblingsblume.jpg',
      },
      colors: new Set<Color>([WHITE, PINK]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
        ],
      }),
    }),
    Fabric.of({
      id: 'koifisch',
      name: 'Koifisch',
      image: {
        url: '/assets/images/fabrics/koifisch.jpg',
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
      id: 'lieblingsblume-mit-gold',
      name: 'Lieblingsblume mit Gold',
      image: {
        url: '/assets/images/fabrics/lieblingsblume_mit_gold.jpg',
      },
      colors: new Set<Color>([PINK, GOLD]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'regenbaer',
      name: 'Regenbär',
      image: {
        url: '/assets/images/fabrics/regenbaer.jpg',
      },
      colors: new Set<Color>([WHITE]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: true }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'rosa',
      name: 'Rosa',
      image: {
        url: '/assets/images/fabrics/rosa.jpg',
      },
      colors: new Set<Color>([PINK]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
        ],
      }),
    }),
    Fabric.of({
      id: 'rosen',
      name: 'Rosen',
      image: {
        url: '/assets/images/fabrics/rosen.jpg',
      },
      colors: new Set<Color>([PINK, WHITE]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
        ],
      }),
    }),
    Fabric.of({
      id: 'vielzupink',
      name: 'VielZuPink',
      image: {
        url: '/assets/images/fabrics/vielzupink.jpg',
      },
      colors: new Set<Color>([PINK, WHITE]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'fuchs-mauve',
      name: 'Fuchs Mauve',
      image: {
        url: '/assets/images/fabrics/fuchs_mauve.jpg',
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
      id: 'fuchs-rauchblau',
      name: 'Fuchs Rauchblau',
      image: {
        url: '/assets/images/fabrics/fuchs_rauchblau.jpg',
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
      id: 'macarons',
      name: 'Macarons',
      image: {
        url: '/assets/images/fabrics/macarons.jpg',
      },
      colors: new Set<Color>([BROWN, BEIGE]),
      themes: new Set<Theme>([SWEETS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
        ],
      }),
    }),
    Fabric.of({
      id: 'fledermaeuschen',
      name: 'Fledermäuschen',
      image: {
        url: '/assets/images/fabrics/fledermaeuschen.jpg',
      },
      colors: new Set<Color>([WHITE, GREY]),
      themes: new Set<Theme>([ANIMALS, HALLOWEEN, FALL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'fuchs-weiss-blau',
      name: 'Fuchs Weiß Blau',
      image: {
        url: '/assets/images/fabrics/fuchs_weiss_blau.jpg',
      },
      colors: new Set<Color>([BLUE, ORANGE, WHITE]),
      themes: new Set<Theme>([ANIMALS, FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: true }),
        ],
      }),
    }),
    Fabric.of({
      id: 'herbsttiere',
      name: 'Herbsttiere',
      image: {
        url: '/assets/images/fabrics/herbsttiere.jpg',
      },
      colors: new Set<Color>([BROWN, ORANGE]),
      themes: new Set<Theme>([ANIMALS, FALL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'kuerbiskatze',
      name: 'Kürbiskatze',
      image: {
        url: '/assets/images/fabrics/kuerbiskatze.jpg',
      },
      colors: new Set<Color>([ORANGE, GREY]),
      themes: new Set<Theme>([ANIMALS, FALL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'loewenkoenig',
      name: 'Löwenkönig',
      image: {
        url: '/assets/images/fabrics/loewenkoenig.jpg',
      },
      colors: new Set<Color>([WHITE, BROWN]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'kuhbaer',
      name: 'Kuhbär',
      image: {
        url: '/assets/images/fabrics/kuhbaer.jpg',
      },
      colors: new Set<Color>([WHITE, BROWN]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'herzchendrache',
      name: 'Herzchendrache',
      image: {
        url: '/assets/images/fabrics/herzchendrache.jpg',
      },
      colors: new Set<Color>([WHITE, PINK, RED]),
      themes: new Set<Theme>([FANTASY]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'lavendelbiene',
      name: 'Lavendelbiene',
      image: {
        url: '/assets/images/fabrics/lavendelbiene.jpg',
      },
      colors: new Set<Color>([WHITE]),
      themes: new Set<Theme>([FLORAL, ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'bienchen',
      name: 'Bienchen',
      image: {
        url: '/assets/images/fabrics/bienchen.jpg',
      },
      colors: new Set<Color>([WHITE, BROWN, BEIGE]),
      themes: new Set<Theme>([ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'blumen-5',
      name: 'Blumen 5',
      image: {
        url: '/assets/images/fabrics/blumen_5.jpg',
      },
      colors: new Set<Color>([BEIGE, RED, PINK]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'blumen-schwarz-1',
      name: 'Blumen schwarz 1',
      image: {
        url: '/assets/images/fabrics/blumen_schwarz_1.jpg',
      },
      colors: new Set<Color>([BLACK, PINK, GOLD]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'blumen-schwarz-2',
      name: 'Blumen schwarz 2',
      image: {
        url: '/assets/images/fabrics/blumen_schwarz_2.jpg',
      },
      colors: new Set<Color>([BLACK, PINK, GOLD]),
      themes: new Set<Theme>([FLORAL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'leoprint-schwarz',
      name: 'Leoprint schwarz 2',
      image: {
        url: '/assets/images/fabrics/leoprint_schwarz.jpg',
      },
      colors: new Set<Color>([BLACK, GOLD]),
      themes: new Set<Theme>([FANTASY, ANIMALS]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'pilzchen',
      name: 'Pilzchen',
      image: {
        url: '/assets/images/fabrics/pilzchen.jpg',
      },
      colors: new Set<Color>([BEIGE, RED]),
      themes: new Set<Theme>([FANTASY, FALL]),
      availability: Availability.of({
        types: [
          TypeAvailability.of({ type: FRENCH_TERRY, inStock: false }),
          TypeAvailability.of({ type: JERSEY, inStock: false }),
        ],
      }),
    }),
    Fabric.of({
      id: 'winternacht',
      name: 'Winternacht',
      image: {
        url: '/assets/images/fabrics/winternacht.jpg',
      },
      colors: new Set<Color>([BLUE]),
      themes: new Set<Theme>([ANIMALS, WINTER]),
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
