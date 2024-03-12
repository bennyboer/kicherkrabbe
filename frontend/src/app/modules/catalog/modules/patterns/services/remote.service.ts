import { Injectable } from '@angular/core';
import { Pattern, PatternExtra, PatternVariant, SizeRange } from '../model';
import { Observable, of } from 'rxjs';
import { Image, Money } from '../../../../../util';

@Injectable()
export class RemotePatternsService {
  private readonly mockPatterns: Pattern[] = [
    Pattern.of({
      id: 'babyshirt',
      name: 'Babyshirt',
      previewImage: Image.of({
        url: '/assets/images/patterns/babyshirt/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/babyshirt/A.jpg',
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
            SizeRange.of({ from: 56, to: 62, price: Money.euro(2000) }),
            SizeRange.of({ from: 68, to: 74, price: Money.euro(2200) }),
            SizeRange.of({ from: 80, to: 86, price: Money.euro(2400) }),
            SizeRange.of({ from: 92, to: 104, price: Money.euro(2600) }),
          ],
        }),
      ],
      attribution: 'RosaRosa',
    }),
    Pattern.of({
      id: 'basic-kleid-die-dritte',
      name: 'Basic Kleid die Dritte',
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
      previewImage: Image.of({
        url: '/assets/images/patterns/dreieckstuch/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/dreieckstuch/A.jpg',
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
          name: 'Lederlabel (Kunstleder)',
          price: Money.euro(200),
        }),
        PatternExtra.of({
          name: 'gestickter Name',
          price: Money.euro(200),
        }),
      ],
    }),
    Pattern.of({
      id: 'knickaboo',
      name: 'Hose Knickaboo',
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
            SizeRange.of({ from: 80, to: 86, price: Money.euro(4400) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(4600) }),
            SizeRange.of({ from: 104, to: 110, price: Money.euro(4800) }),
            SizeRange.of({ from: 116, to: 116, price: Money.euro(5000) }),
          ],
        }),
        PatternVariant.of({
          id: 'top-length',
          name: 'Toplänge',
          sizes: [
            SizeRange.of({ from: 80, to: 86, price: Money.euro(3800) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(4000) }),
            SizeRange.of({ from: 104, to: 110, price: Money.euro(4200) }),
            SizeRange.of({ from: 116, to: 116, price: Money.euro(4400) }),
          ],
        }),
      ],
      extras: [
        PatternExtra.of({
          name: 'Fake Knopfleiste (mit Knöpfen und Spitze)',
          price: Money.euro(500),
        }),
        PatternExtra.of({
          name: 'Kurze Puffärmel',
          price: Money.euro(-400),
        }),
      ],
      attribution: 'LLK',
    }),
    Pattern.of({
      id: 'muetzzel',
      name: 'Mützzel',
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
      id: 'pumphose-penny',
      name: 'Pumphose Penny',
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
      attribution: 'Libminna',
    }),
    Pattern.of({
      id: 'sommerkleid-yuna',
      name: 'Sommerkleid Yuna',
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
            SizeRange.of({ from: 62, to: 74, price: Money.euro(3000) }),
            SizeRange.of({ from: 80, to: 86, price: Money.euro(3200) }),
            SizeRange.of({ from: 92, to: 98, price: Money.euro(3400) }),
            SizeRange.of({ from: 104, to: 116, price: Money.euro(3600) }),
          ],
        }),
      ],
      attribution: 'Herr Knirps',
    }),
    Pattern.of({
      id: 'stracciatella',
      name: 'Top & Kleid Stracciatella',
      previewImage: Image.of({
        url: '/assets/images/patterns/stracciatella/preview.jpg',
      }),
      images: [
        Image.of({
          url: '/assets/images/patterns/stracciatella/A.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/stracciatella/B.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/stracciatella/C.jpg',
        }),
        Image.of({
          url: '/assets/images/patterns/stracciatella/D.jpg',
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
  ];

  getPatterns(): Observable<Pattern[]> {
    return of(this.mockPatterns);
  }
}
