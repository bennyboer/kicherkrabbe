import { Injectable } from '@angular/core';
import { Pattern, PatternExtra, PatternVariant, SizeRange } from '../model';
import { Observable, of } from 'rxjs';
import { Image, Money } from '../../../../../util';
import { ACCESSORY, DRESS, ONESIE, PANTS, TOP } from '../model/category';

@Injectable()
export class RemotePatternsService {
  private readonly mockPatterns: Pattern[] = [
    Pattern.of({
      id: 'babyshirt',
      name: 'Babyshirt',
      categories: new Set([TOP]),
      previewImage: Image.of({
        url: '/assets/images/patterns/babyshirt/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/babyshirt/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/babyshirt/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/babyshirt/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/babyshirt/D.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'basic',
          name: 'Basic',
          sizes: [
            SizeRange.of({ from: 56, to: 62, price: Money.euro(1800) }),
            SizeRange.of({ from: 68, to: 74, price: Money.euro(2000) }),
            SizeRange.of({ from: 80, to: 86, price: Money.euro(2200) }),
            SizeRange.of({ from: 92, to: 104, price: Money.euro(2400) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Lange Ärmel',
          price: Money.euro(200),
        }),
      ],
      attribution: 'RosaRosa',
    }),
    Pattern.of({
      id: 'basic-kleid-die-dritte',
      name: 'Basic Kleid die Dritte',
      categories: new Set([DRESS]),
      previewImage: Image.of({
        url: '/assets/images/patterns/basic-kleid-die-dritte/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/basic-kleid-die-dritte/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/basic-kleid-die-dritte/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/basic-kleid-die-dritte/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/basic-kleid-die-dritte/D.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/basic-kleid-die-dritte/E.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'basic',
          name: 'Basic',
          sizes: [
            SizeRange.of({ from: 80, to: 86, price: Money.euro(3300) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(3500) }),
            SizeRange.of({ from: 104, to: 110, price: Money.euro(3700) }),
            SizeRange.of({ from: 116, to: 116, price: Money.euro(3900) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Rollkragen',
          price: Money.euro(500),
        }),
      ],
      attribution: 'kiOo kiOo',
    }),
    Pattern.of({
      id: 'basic-sweater-der-fuenfte',
      name: 'Basic Sweater der Fünfte',
      categories: new Set([TOP]),
      previewImage: Image.of({
        url: '/assets/images/patterns/basic-sweater-der-fuenfte/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/basic-sweater-der-fuenfte/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/basic-sweater-der-fuenfte/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/basic-sweater-der-fuenfte/C.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'basic',
          name: 'Basic',
          sizes: [
            SizeRange.of({ from: 80, to: 80, price: Money.euro(2500) }),
            SizeRange.of({ from: 86, to: 92, price: Money.euro(2700) }),
            SizeRange.of({ from: 98, to: 104, price: Money.euro(2900) }),
            SizeRange.of({ from: 110, to: 116, price: Money.euro(3100) }),
          ],
        }),
        PatternVariant.of({
          id: 'fake-undershirt',
          name: 'Fake-Druntershirt',
          sizes: [
            SizeRange.of({ from: 80, to: 80, price: Money.euro(2700) }),
            SizeRange.of({ from: 86, to: 92, price: Money.euro(2900) }),
            SizeRange.of({ from: 98, to: 104, price: Money.euro(3100) }),
            SizeRange.of({ from: 110, to: 116, price: Money.euro(3300) }),
          ],
        }),
        PatternVariant.of({
          id: 'girly',
          name: 'Girly',
          sizes: [
            SizeRange.of({ from: 80, to: 80, price: Money.euro(2900) }),
            SizeRange.of({ from: 86, to: 92, price: Money.euro(3100) }),
            SizeRange.of({ from: 98, to: 104, price: Money.euro(3300) }),
            SizeRange.of({ from: 110, to: 116, price: Money.euro(3500) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Ärmel im Lagenlook',
          price: Money.zero(),
        }),
        PatternExtra.of({
          name: 'Brusttasche',
          price: Money.euro(100),
        }),
        PatternExtra.of({
          name: 'Bauchtasche',
          price: Money.euro(200),
        }),
      ],
      attribution: 'kiOo kiOo',
    }),
    Pattern.of({
      id: 'dreieckstuch',
      name: 'Dreieckstuch',
      categories: new Set([ACCESSORY]),
      previewImage: Image.of({
        url: '/assets/images/patterns/dreieckstuch/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/dreieckstuch/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/dreieckstuch/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/dreieckstuch/C.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'basic',
          name: 'Basic',
          sizes: [
            SizeRange.of({
              from: 0,
              to: 2,
              unit: 'Jahre',
              price: Money.euro(700),
            }),
            SizeRange.of({
              from: 2,
              unit: 'Jahre',
              price: Money.euro(750),
            }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Satin Schleife',
          price: Money.euro(100),
        }),
        PatternExtra.of({
          name: 'gestickter Name',
          price: Money.euro(200),
        }),
        PatternExtra.of({
          name: 'Applikationsstickerei',
          price: Money.euro(500),
        }),
        PatternExtra.of({
          name: 'Lederlabel',
          price: Money.euro(200),
        }),
        PatternExtra.of({
          name: 'Label',
          price: Money.euro(100),
        }),
      ],
    }),
    Pattern.of({
      id: 'knickaboo',
      name: 'Hose Knickaboo',
      categories: new Set([PANTS]),
      previewImage: Image.of({
        url: '/assets/images/patterns/knickaboo/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/knickaboo/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/knickaboo/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/knickaboo/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/knickaboo/D.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/knickaboo/E.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'basic',
          name: 'Basic',
          sizes: [
            SizeRange.of({ from: 56, to: 62, price: Money.euro(2600) }),
            SizeRange.of({ from: 68, to: 74, price: Money.euro(2800) }),
            SizeRange.of({ from: 80, to: 86, price: Money.euro(3000) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(3200) }),
            SizeRange.of({ from: 104, to: 110, price: Money.euro(3400) }),
            SizeRange.of({ from: 116, to: 116, price: Money.euro(3600) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Bündchenabschluss oder gesäumt',
          price: Money.zero(),
        }),
        PatternExtra.of({
          name: 'Kordel',
          price: Money.euro(100),
        }),
      ],
      attribution: 'RosaRosa',
    }),
    Pattern.of({
      id: 'kleid-heidi',
      name: 'Kleid Heidi',
      categories: new Set([DRESS]),
      previewImage: Image.of({
        url: '/assets/images/patterns/kleid-heidi/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/kleid-heidi/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/kleid-heidi/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/kleid-heidi/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/kleid-heidi/D.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/kleid-heidi/E.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'basic',
          name: 'Basic',
          sizes: [
            SizeRange.of({ from: 80, to: 86, price: Money.euro(4400) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(4600) }),
            SizeRange.of({ from: 104, to: 110, price: Money.euro(4800) }),
            SizeRange.of({ from: 116, to: 116, price: Money.euro(5000) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Rollkragen oder Kapuze',
          price: Money.zero(),
        }),
        PatternExtra.of({
          name: 'Saumrüsche',
          price: Money.euro(300),
        }),
        PatternExtra.of({
          name: 'Satinschleife',
          price: Money.euro(100),
        }),
      ],
      attribution: 'emmilou.',
    }),
    Pattern.of({
      id: 'kleid-morgan',
      name: 'Kleid Morgan',
      categories: new Set([DRESS]),
      previewImage: Image.of({
        url: '/assets/images/patterns/kleid-morgan/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/kleid-morgan/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/kleid-morgan/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/kleid-morgan/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/kleid-morgan/D.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'normal',
          name: 'Normal',
          sizes: [
            SizeRange.of({ from: 80, to: 86, price: Money.euro(4000) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(4200) }),
            SizeRange.of({ from: 104, to: 110, price: Money.euro(4400) }),
            SizeRange.of({ from: 116, to: 116, price: Money.euro(4600) }),
          ],
        }),
        PatternVariant.of({
          id: 'top-length',
          name: 'Toplänge',
          sizes: [
            SizeRange.of({ from: 80, to: 86, price: Money.euro(3400) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(3600) }),
            SizeRange.of({ from: 104, to: 110, price: Money.euro(3800) }),
            SizeRange.of({ from: 116, to: 116, price: Money.euro(4000) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Fake Knopfleiste (mit Knöpfen und Spitze)',
          price: Money.euro(300),
        }),
        PatternExtra.of({
          name: 'Lange Puffärmel',
          price: Money.euro(400),
        }),
      ],
      attribution: 'LLK',
    }),
    Pattern.of({
      id: 'muetzzel',
      name: 'Mützzel',
      categories: new Set([ACCESSORY]),
      previewImage: Image.of({
        url: '/assets/images/patterns/muetzzel/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/muetzzel/A.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'basic',
          name: 'Basic',
          sizes: [
            SizeRange.of({
              from: 35,
              to: 44,
              unit: 'cm Kopfumfang',
              price: Money.euro(1300),
            }),
            SizeRange.of({
              from: 45,
              to: 54,
              unit: 'cm Kopfumfang',
              price: Money.euro(1500),
            }),
          ],
        }),
      ],
      attribution: 'Jo Mina',
    }),
    Pattern.of({
      id: 'pumphose',
      name: 'Pumphose',
      categories: new Set([PANTS]),
      previewImage: Image.of({
        url: '/assets/images/patterns/pumphose-penny/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/pumphose-penny/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/pumphose-penny/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/pumphose-penny/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/pumphose-penny/D.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'basic',
          name: 'Basic',
          sizes: [
            SizeRange.of({ from: 56, to: 56, price: Money.euro(1600) }),
            SizeRange.of({ from: 62, to: 68, price: Money.euro(1800) }),
            SizeRange.of({ from: 74, to: 80, price: Money.euro(2000) }),
            SizeRange.of({ from: 86, to: 92, price: Money.euro(2200) }),
            SizeRange.of({ from: 98, to: 104, price: Money.euro(2400) }),
            SizeRange.of({ from: 110, to: 116, price: Money.euro(2600) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Taschen',
          price: Money.euro(200),
        }),
      ],
    }),
    Pattern.of({
      id: 'sommerkleid-yuna',
      name: 'Sommerkleid Yuna',
      categories: new Set([DRESS]),
      previewImage: Image.of({
        url: '/assets/images/patterns/sommerkleid-yuna/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/sommerkleid-yuna/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/sommerkleid-yuna/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/sommerkleid-yuna/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/sommerkleid-yuna/D.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'tunika',
          name: 'Tunika',
          sizes: [
            SizeRange.of({ from: 62, to: 74, price: Money.euro(2300) }),
            SizeRange.of({ from: 80, to: 86, price: Money.euro(2500) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(2700) }),
            SizeRange.of({ from: 104, to: 116, price: Money.euro(2900) }),
          ],
        }),
        PatternVariant.of({
          id: 'kleid',
          name: 'Kleid',
          sizes: [
            SizeRange.of({ from: 62, to: 74, price: Money.euro(2800) }),
            SizeRange.of({ from: 80, to: 86, price: Money.euro(3000) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(3200) }),
            SizeRange.of({ from: 104, to: 116, price: Money.euro(3400) }),
          ],
        }),
      ],
      attribution: 'Herr Knirps',
    }),
    Pattern.of({
      id: 'stracciatella',
      name: 'Top & Kleid Stracciatella',
      categories: new Set([TOP, DRESS]),
      previewImage: Image.of({
        url: '/assets/images/patterns/stracciatella/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/stracciatella/Z.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/stracciatella/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/stracciatella/E.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'top',
          name: 'Top',
          sizes: [
            SizeRange.of({ from: 80, to: 86, price: Money.euro(2300) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(2500) }),
            SizeRange.of({ from: 104, to: 110, price: Money.euro(2700) }),
            SizeRange.of({ from: 116, to: 116, price: Money.euro(2900) }),
          ],
        }),
        PatternVariant.of({
          id: 'tunika',
          name: 'Tunika',
          sizes: [
            SizeRange.of({ from: 80, to: 86, price: Money.euro(2800) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(3000) }),
            SizeRange.of({ from: 104, to: 110, price: Money.euro(3200) }),
            SizeRange.of({ from: 116, to: 116, price: Money.euro(3400) }),
          ],
        }),
        PatternVariant.of({
          id: 'kleid',
          name: 'Kleid',
          sizes: [
            SizeRange.of({ from: 80, to: 86, price: Money.euro(3400) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(3600) }),
            SizeRange.of({ from: 104, to: 110, price: Money.euro(3800) }),
            SizeRange.of({ from: 116, to: 116, price: Money.euro(4000) }),
          ],
        }),
      ],
      attribution: 'LEMELdesign',
    }),
    Pattern.of({
      id: 'latzhose',
      name: 'Latzhose',
      categories: new Set([PANTS, ONESIE]),
      previewImage: Image.of({
        url: '/assets/images/patterns/latzhose/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/latzhose/E.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/latzhose/F.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/latzhose/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/latzhose/D.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/latzhose/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/latzhose/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/latzhose/G.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'normal',
          name: 'Normal',
          sizes: [
            SizeRange.of({ from: 50, to: 56, price: Money.euro(2800) }),
            SizeRange.of({ from: 62, to: 68, price: Money.euro(3000) }),
            SizeRange.of({ from: 74, to: 80, price: Money.euro(3200) }),
            SizeRange.of({ from: 86, to: 92, price: Money.euro(3400) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Stickerei',
          price: Money.euro(1200),
        }),
      ],
      originalPatternName: 'Latzhose Oxford',
      attribution: 'Seidenseele',
    }),
    Pattern.of({
      id: 'easy-peasy-pants',
      name: 'Easy Peasy Pants',
      categories: new Set([PANTS]),
      previewImage: Image.of({
        url: '/assets/images/patterns/easy-peasy-pants/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/easy-peasy-pants/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/easy-peasy-pants/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/easy-peasy-pants/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/easy-peasy-pants/D.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'short',
          name: 'Kurze',
          sizes: [
            SizeRange.of({ from: 56, to: 62, price: Money.euro(2000) }),
            SizeRange.of({ from: 68, to: 74, price: Money.euro(2200) }),
            SizeRange.of({ from: 80, to: 86, price: Money.euro(2400) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(2600) }),
            SizeRange.of({ from: 104, to: 104, price: Money.euro(2800) }),
          ],
        }),
        PatternVariant.of({
          id: 'knee-long',
          name: 'Knielang',
          sizes: [
            SizeRange.of({ from: 56, to: 62, price: Money.euro(2300) }),
            SizeRange.of({ from: 68, to: 74, price: Money.euro(2500) }),
            SizeRange.of({ from: 80, to: 86, price: Money.euro(2700) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(2900) }),
            SizeRange.of({ from: 104, to: 104, price: Money.euro(3100) }),
          ],
        }),
        PatternVariant.of({
          id: 'long',
          name: 'Lang',
          sizes: [
            SizeRange.of({ from: 56, to: 62, price: Money.euro(2600) }),
            SizeRange.of({ from: 68, to: 74, price: Money.euro(2800) }),
            SizeRange.of({ from: 80, to: 86, price: Money.euro(3000) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(3200) }),
            SizeRange.of({ from: 104, to: 104, price: Money.euro(3400) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Gummi- oder Bündchenbund',
          price: Money.zero(),
        }),
        PatternExtra.of({
          name: 'Fake Knopfleiste',
          price: Money.zero(),
        }),
        PatternExtra.of({
          name: 'Fake Hosenträger',
          price: Money.zero(),
        }),
        PatternExtra.of({
          name: 'Aufgesetzte Hosen- und Potaschen',
          price: Money.zero(),
        }),
        PatternExtra.of({
          name: 'Richtige Hosenträger',
          price: Money.euro(100),
        }),
      ],
      originalPatternName: 'Easy Peasy Pants',
      attribution: 'Herzklee Design',
    }),
    Pattern.of({
      id: 'strampler',
      name: 'Strampler',
      categories: new Set([ONESIE]),
      previewImage: Image.of({
        url: '/assets/images/patterns/strampler/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/strampler/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/strampler/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/strampler/C.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'normal',
          name: 'Normal',
          sizes: [
            SizeRange.of({ from: 56, to: 62, price: Money.euro(2700) }),
            SizeRange.of({ from: 68, to: 74, price: Money.euro(2900) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Rüsche',
          price: Money.zero(),
        }),
        PatternExtra.of({
          name: 'Stickerei',
          price: Money.euro(1200),
        }),
      ],
      originalPatternName: 'Tigerstrampler',
      attribution: 'Tigerlilly',
    }),
    Pattern.of({
      id: 'sommer-romper',
      name: 'Sommerromper',
      categories: new Set([ONESIE]),
      previewImage: Image.of({
        url: '/assets/images/patterns/sommer-romper/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/sommer-romper/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/sommer-romper/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/sommer-romper/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/sommer-romper/D.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'short',
          name: 'Kurz',
          sizes: [
            SizeRange.of({ from: 56, to: 62, price: Money.euro(3200) }),
            SizeRange.of({ from: 68, to: 74, price: Money.euro(3400) }),
            SizeRange.of({ from: 80, to: 86, price: Money.euro(3600) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(3800) }),
          ],
        }),
        PatternVariant.of({
          id: 'long',
          name: 'Lang',
          sizes: [
            SizeRange.of({ from: 56, to: 62, price: Money.euro(3500) }),
            SizeRange.of({ from: 68, to: 74, price: Money.euro(3700) }),
            SizeRange.of({ from: 80, to: 86, price: Money.euro(3900) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(4100) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Fake Knopfleiste',
          price: Money.euro(300),
        }),
        PatternExtra.of({
          name: 'Schleife zum Binden oder Knöpfe',
          price: Money.zero(),
        }),
      ],
      originalPatternName: 'Baby Festival Combo',
      attribution: 'Tadah Patterns',
    }),
    Pattern.of({
      id: 'sommer-kleid',
      name: 'Sommerkleid',
      categories: new Set([DRESS]),
      previewImage: Image.of({
        url: '/assets/images/patterns/sommer-kleid/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/sommer-kleid/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/sommer-kleid/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/sommer-kleid/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/sommer-kleid/D.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'onestep',
          name: 'Einstufig',
          sizes: [
            SizeRange.of({ from: 56, to: 62, price: Money.euro(3000) }),
            SizeRange.of({ from: 68, to: 74, price: Money.euro(3200) }),
            SizeRange.of({ from: 80, to: 86, price: Money.euro(3400) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(3600) }),
            SizeRange.of({ from: 104, to: 110, price: Money.euro(3800) }),
            SizeRange.of({ from: 116, to: 116, price: Money.euro(4000) }),
          ],
        }),
        PatternVariant.of({
          id: 'twostep',
          name: 'Zweistufig',
          sizes: [
            SizeRange.of({ from: 56, to: 62, price: Money.euro(3300) }),
            SizeRange.of({ from: 68, to: 74, price: Money.euro(3500) }),
            SizeRange.of({ from: 80, to: 86, price: Money.euro(3700) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(3900) }),
            SizeRange.of({ from: 104, to: 110, price: Money.euro(4100) }),
            SizeRange.of({ from: 116, to: 116, price: Money.euro(4300) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Fake Knopfleiste',
          price: Money.euro(300),
        }),
        PatternExtra.of({
          name: 'Schleife zum Binden oder Knöpfe',
          price: Money.zero(),
        }),
      ],
      originalPatternName: 'Baby Festival Combo',
      attribution: 'Tadah Patterns',
    }),
    Pattern.of({
      id: 'rueschentuch',
      name: 'Rüschentuch',
      categories: new Set([ACCESSORY]),
      previewImage: Image.of({
        url: '/assets/images/patterns/rueschentuch/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/rueschentuch/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/rueschentuch/B.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'normal',
          name: 'Normal',
          sizes: [
            SizeRange.of({
              from: 0,
              to: 2,
              unit: 'Jahre',
              price: Money.euro(1000),
            }),
            SizeRange.of({ from: 2, unit: 'Jahre', price: Money.euro(1100) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Satin Schleife',
          price: Money.euro(100),
        }),
        PatternExtra.of({
          name: 'gestickter Name',
          price: Money.euro(200),
        }),
        PatternExtra.of({
          name: 'Applikationsstickerei',
          price: Money.euro(500),
        }),
        PatternExtra.of({
          name: 'Lederlabel',
          price: Money.euro(200),
        }),
        PatternExtra.of({
          name: 'Label',
          price: Money.euro(100),
        }),
      ],
    }),
    Pattern.of({
      id: 'oversized-shirt',
      name: 'Oversized Shirt',
      categories: new Set([TOP]),
      previewImage: Image.of({
        url: '/assets/images/patterns/oversized-shirt/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/oversized-shirt/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/oversized-shirt/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/oversized-shirt/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/oversized-shirt/D.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/oversized-shirt/E.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/oversized-shirt/F.jpg',
        }),
      ],
      variants: [
        PatternVariant.of({
          id: 'normal',
          name: 'Normal',
          sizes: [
            SizeRange.of({ from: 62, to: 68, price: Money.euro(1800) }),
            SizeRange.of({ from: 74, to: 80, price: Money.euro(2000) }),
            SizeRange.of({ from: 86, to: 92, price: Money.euro(2200) }),
            SizeRange.of({ from: 98, to: 104, price: Money.euro(2400) }),
            SizeRange.of({ from: 110, to: 116, price: Money.euro(2600) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Lange Ärmel',
          price: Money.euro(200),
        }),
        PatternExtra.of({
          name: 'Bauchtasche',
          price: Money.euro(200),
        }),
      ],
      attribution: 'Michelkids',
      originalPatternName: 'Simply Shirt',
    }),
  ];

  getPatterns(): Observable<Pattern[]> {
    return of(this.mockPatterns);
  }
}
