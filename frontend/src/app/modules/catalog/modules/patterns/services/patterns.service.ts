import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import {
  Pattern,
  PatternAttribution,
  PatternExtra,
  PatternVariant,
  PricedSizeRange,
} from '../model';
import { Currency, Money, someOrNone } from '../../../../../util';
import { environment } from '../../../../../../environments';

interface PatternsSortDTO {
  property: PatternsSortPropertyDTO;
  direction: PatternsSortDirectionDTO;
}

enum PatternsSortPropertyDTO {
  ALPHABETICAL = 'ALPHABETICAL',
}

enum PatternsSortDirectionDTO {
  ASCENDING = 'ASCENDING',
  DESCENDING = 'DESCENDING',
}

interface QueryPublishedPatternsRequest {
  searchTerm: string;
  categories: string[];
  sizes: number[];
  sort: PatternsSortDTO;
  skip: number;
  limit: number;
}

interface QueryPublishedPatternsResponse {
  skip: number;
  limit: number;
  total: number;
  patterns: PublishedPatternDTO[];
}

interface QueryPublishedPatternResponse {
  pattern: PublishedPatternDTO;
}

interface PublishedPatternDTO {
  id: string;
  name: string;
  description?: string;
  alias: string;
  attribution: PatternAttributionDTO;
  categories: string[];
  images: string[];
  variants: PatternVariantDTO[];
  extras: PatternExtraDTO[];
}

interface PatternAttributionDTO {
  originalPatternName?: string;
  designer?: string;
}

interface PatternVariantDTO {
  name: string;
  pricedSizeRanges: PricedSizeRangeDTO[];
}

interface PricedSizeRangeDTO {
  from: number;
  to?: number | null;
  unit?: string | null;
  price: MoneyDTO;
}

interface MoneyDTO {
  amount: number;
  currency: string;
}

interface PatternExtraDTO {
  name: string;
  price: MoneyDTO;
}

@Injectable()
export class PatternsService {
  constructor(private readonly http: HttpClient) {}

  getPatterns(props: {
    searchTerm?: string;
    categories?: Set<string>;
    sizes?: Set<number>;
    ascending?: boolean;
    skip?: number;
    limit?: number;
  }): Observable<Pattern[]> {
    const request: QueryPublishedPatternsRequest = {
      searchTerm: someOrNone(props.searchTerm)
        .map((s) => s.trim())
        .filter((s) => s.length > 0)
        .orElse(''),
      categories: someOrNone(props.categories)
        .map((c) => Array.from(c))
        .orElse([]),
      sizes: someOrNone(props.sizes)
        .map((s) => Array.from(s))
        .orElse([]),
      sort: {
        property: PatternsSortPropertyDTO.ALPHABETICAL,
        direction: someOrNone(props.ascending)
          .map((a) =>
            a
              ? PatternsSortDirectionDTO.ASCENDING
              : PatternsSortDirectionDTO.DESCENDING,
          )
          .orElse(PatternsSortDirectionDTO.ASCENDING),
      },
      skip: someOrNone(props.skip).orElse(0),
      limit: someOrNone(props.limit).orElse(100),
    };

    return this.http
      .post<QueryPublishedPatternsResponse>(
        `${environment.apiUrl}/patterns/published`,
        request,
      )
      .pipe(
        map((response) =>
          response.patterns.map((pattern) => this.toInternalPattern(pattern)),
        ),
      );
  }

  getPattern(id: string): Observable<Pattern> {
    return this.http
      .get<QueryPublishedPatternResponse>(
        `${environment.apiUrl}/patterns/${id}/published`,
      )
      .pipe(map((response) => this.toInternalPattern(response.pattern)));
  }

  private toInternalPattern(pattern: PublishedPatternDTO): Pattern {
    return Pattern.of({
      id: pattern.id,
      name: pattern.name,
      description: pattern.description,
      alias: pattern.alias,
      attribution: this.toInternalAttribution(pattern.attribution),
      categories: new Set<string>(pattern.categories),
      images: pattern.images,
      variants: pattern.variants.map((variant) =>
        this.toInternalVariant(variant),
      ),
      extras: pattern.extras.map((extra) => this.toInternalExtra(extra)),
    });
  }

  private toInternalAttribution(
    attribution: PatternAttributionDTO,
  ): PatternAttribution {
    return PatternAttribution.of({
      originalPatternName: attribution.originalPatternName,
      designer: attribution.designer,
    });
  }

  private toInternalVariant(variant: PatternVariantDTO): PatternVariant {
    const sizes = variant.pricedSizeRanges.map((range) =>
      this.toInternalPricedSizeRange(range),
    );
    sizes.sort((a, b) => a.from - b.from);

    return PatternVariant.of({
      name: variant.name,
      sizes,
    });
  }

  private toInternalPricedSizeRange(
    range: PricedSizeRangeDTO,
  ): PricedSizeRange {
    return PricedSizeRange.of({
      from: range.from,
      to: range.to,
      unit: range.unit,
      price: this.toInternalMoney(range.price),
    });
  }

  private toInternalMoney(price: MoneyDTO): Money {
    return Money.of({
      value: price.amount,
      currency: this.toInternalCurrency(price.currency),
    });
  }

  private toInternalCurrency(currency: string): Currency {
    switch (currency) {
      case 'EUR':
        return Currency.euro();
      default:
        throw new Error(`Unknown currency: ${currency}`);
    }
  }

  private toInternalExtra(extra: PatternExtraDTO): PatternExtra {
    return PatternExtra.of({
      name: extra.name,
      price: this.toInternalMoney(extra.price),
    });
  }
}
