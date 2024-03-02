import { Injectable } from '@angular/core';
import { Pattern, PatternExtra, PatternVariant, SizeRange } from '../model';
import { delay, Observable, of } from 'rxjs';
import { Image, Money } from '../../../../../util';

@Injectable()
export class RemotePatternsService {
  private readonly mockPatterns: Pattern[] = Array(10)
    .fill(0)
    .map((_, i) =>
      Pattern.of({
        id: i.toString(),
        name: 'Basic Sweater der Fünfte',
        images: [
          Image.of({ url: '/assets/examples/example.jpg' }),
          Image.of({ url: '/assets/examples/example.jpg' }),
          Image.of({ url: '/assets/examples/example.jpg' }),
          Image.of({ url: '/assets/examples/example.jpg' }),
          Image.of({ url: '/assets/examples/example.jpg' }),
        ],
        variants: [
          PatternVariant.of({
            id: 'basic',
            name: 'Basic Variante',
            sizes: [
              SizeRange.of({ from: 80, to: 80, price: Money.euro(2500) }),
              SizeRange.of({ from: 86, to: 92, price: Money.euro(2700) }),
              SizeRange.of({ from: 98, to: 104, price: Money.euro(2900) }),
              SizeRange.of({ from: 110, to: 116, price: Money.euro(3100) }),
            ],
          }),
          PatternVariant.of({
            id: 'fake-undershirt',
            name: 'Fake-Druntershirt Variante',
            sizes: [
              SizeRange.of({ from: 80, to: 80, price: Money.euro(2700) }),
              SizeRange.of({ from: 86, to: 92, price: Money.euro(2900) }),
              SizeRange.of({ from: 98, to: 104, price: Money.euro(3100) }),
              SizeRange.of({ from: 110, to: 116, price: Money.euro(3300) }),
            ],
          }),
          PatternVariant.of({
            id: 'girly',
            name: 'Girly Variante',
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
    );

  // TODO use real backend API

  getPatterns(): Observable<Pattern[]> {
    return of(this.mockPatterns).pipe(delay(300));
  }
}
