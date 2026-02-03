import { Injectable, OnDestroy } from '@angular/core';
import { filter, map, Observable, Subject, takeUntil } from 'rxjs';
import { Currency, Money } from '../../../../../util';
import { SSE } from 'sse.js';
import { HttpClient } from '@angular/common/http';
import { AdminAuthService } from '../../../services';
import { environment } from '../../../../../../environments';
import {
  Pattern,
  PatternAttribution,
  PatternCategoryId,
  PatternExtra,
  PatternId,
  PatternVariant,
  PricedSizeRange,
} from '../model';
import { none, Option, some, someOrNone } from '@kicherkrabbe/shared';

interface PatternDTO {
  id: string;
  version: number;
  published: boolean;
  featured: boolean;
  name: string;
  number: string;
  description?: string;
  attribution: PatternAttributionDTO;
  categories: string[];
  images: string[];
  variants: PatternVariantDTO[];
  extras: PatternExtraDTO[];
  createdAt: string;
}

interface PatternAttributionDTO {
  originalPatternName?: string;
  designer?: string;
}

interface PatternExtraDTO {
  name: string;
  price: MoneyDTO;
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

interface QueryPatternResponse {
  pattern: PatternDTO;
}

interface QueryPatternsRequest {
  searchTerm: string;
  categories: string[];
  skip: number;
  limit: number;
}

interface QueryPatternsResponse {
  skip: number;
  limit: number;
  total: number;
  patterns: PatternDTO[];
}

interface CreatePatternRequest {
  name: string;
  number: string;
  description?: string;
  attribution: PatternAttributionDTO;
  categories: string[];
  images: string[];
  variants: PatternVariantDTO[];
  extras: PatternExtraDTO[];
}

interface RenamePatternRequest {
  version: number;
  name: string;
}

interface RenamePatternResponse {
  version: number;
}

interface UpdatePatternNumberRequest {
  version: number;
  number: string;
}

interface UpdatePatternNumberResponse {
  version: number;
}

interface UpdateImagesRequest {
  version: number;
  images: string[];
}

interface UpdateImagesResponse {
  version: number;
}

interface UpdateAttributionRequest {
  version: number;
  attribution: PatternAttributionDTO;
}

interface UpdateAttributionResponse {
  version: number;
}

interface UpdateCategoriesRequest {
  version: number;
  categories: string[];
}

interface UpdateCategoriesResponse {
  version: number;
}

interface UpdateVariantsRequest {
  version: number;
  variants: PatternVariantDTO[];
}

interface UpdateVariantsResponse {
  version: number;
}

interface UpdateExtrasRequest {
  version: number;
  extras: PatternExtraDTO[];
}

interface UpdateExtrasResponse {
  version: number;
}

interface UpdateDescriptionRequest {
  description?: string;
  version: number;
}

interface UpdateDescriptionResponse {
  version: number;
}

interface PublishPatternResponse {
  version: number;
}

interface UnpublishPatternResponse {
  version: number;
}

interface FeaturePatternResponse {
  version: number;
}

interface UnfeaturePatternResponse {
  version: number;
}

interface CreatePatternResponse {
  id: string;
}

interface PatternChangeDTO {
  type: string;
  affected: string[];
  payload: any;
}

@Injectable()
export class PatternsService implements OnDestroy {
  private readonly events$: Subject<PatternChangeDTO> = new Subject<PatternChangeDTO>();
  private readonly destroy$: Subject<void> = new Subject<void>();

  private sse: Option<SSE> = none();

  constructor(
    private readonly http: HttpClient,
    private readonly authService: AdminAuthService,
  ) {
    const loggedOut$ = this.authService.getToken().pipe(filter((token) => token.isNone()));
    loggedOut$.pipe(takeUntil(this.destroy$)).subscribe(() => this.closeEventStream());
  }

  ngOnDestroy(): void {
    this.events$.complete();

    this.destroy$.next();
    this.destroy$.complete();

    this.closeEventStream();
  }

  getPatternChanges(): Observable<Set<PatternId>> {
    this.makeSureEventStreamIsOpen();

    return this.events$.pipe(
      filter((event) => event.type !== 'CREATED'),
      map((event) => new Set<PatternId>(event.affected)),
    );
  }

  getPattern(id: PatternId): Observable<Pattern> {
    return this.http
      .get<QueryPatternResponse>(`${environment.apiUrl}/patterns/${id}`)
      .pipe(map((response) => this.toInternalPattern(response.pattern)));
  }

  getPatterns(props: {
    searchTerm?: string;
    categories?: string[];
    skip?: number;
    limit?: number;
  }): Observable<Pattern[]> {
    const searchTerm = someOrNone(props.searchTerm).orElse('');
    const categories = someOrNone(props.categories).orElse([]);
    const skip = someOrNone(props.skip).orElse(0);
    const limit = someOrNone(props.limit).orElse(100);

    const request: QueryPatternsRequest = {
      searchTerm,
      categories,
      skip,
      limit,
    };

    return this.http
      .post<QueryPatternsResponse>(`${environment.apiUrl}/patterns`, request)
      .pipe(map((response) => response.patterns.map((p) => this.toInternalPattern(p))));
  }

  createPattern(props: {
    name: string;
    number: string;
    description?: string | null;
    attribution: PatternAttribution;
    categories: PatternCategoryId[];
    images: string[];
    variants: PatternVariant[];
    extras: PatternExtra[];
  }): Observable<PatternId> {
    const request: CreatePatternRequest = {
      name: props.name,
      number: props.number,
      attribution: this.toApiAttribution(props.attribution),
      categories: props.categories,
      images: props.images,
      variants: props.variants.map((variant) => this.toApiVariant(variant)),
      extras: props.extras.map((extra) => this.toApiExtra(extra)),
    };

    someOrNone(props.description).ifSome((description) => {
      request.description = description;
    });

    return this.http
      .post<CreatePatternResponse>(`${environment.apiUrl}/patterns/create`, request)
      .pipe(map((response) => response.id));
  }

  renamePattern(id: PatternId, version: number, name: string): Observable<number> {
    const request: RenamePatternRequest = { version, name };

    return this.http
      .post<RenamePatternResponse>(`${environment.apiUrl}/patterns/${id}/rename`, request)
      .pipe(map((response) => response.version));
  }

  updatePatternNumber(id: PatternId, version: number, number: string): Observable<number> {
    const request: UpdatePatternNumberRequest = { version, number };

    return this.http
      .post<UpdatePatternNumberResponse>(`${environment.apiUrl}/patterns/${id}/update/number`, request)
      .pipe(map((response) => response.version));
  }

  updateImages(id: PatternId, version: number, images: string[]): Observable<number> {
    const request: UpdateImagesRequest = { version, images };

    return this.http
      .post<UpdateImagesResponse>(`${environment.apiUrl}/patterns/${id}/update/images`, request)
      .pipe(map((response) => response.version));
  }

  updateAttribution(id: PatternId, version: number, attribution: PatternAttribution): Observable<number> {
    const request: UpdateAttributionRequest = {
      version,
      attribution: this.toApiAttribution(attribution),
    };

    return this.http
      .post<UpdateAttributionResponse>(`${environment.apiUrl}/patterns/${id}/update/attribution`, request)
      .pipe(map((response) => response.version));
  }

  updateCategories(id: PatternId, version: number, categories: PatternCategoryId[]): Observable<number> {
    const request: UpdateCategoriesRequest = { version, categories };

    return this.http
      .post<UpdateCategoriesResponse>(`${environment.apiUrl}/patterns/${id}/update/categories`, request)
      .pipe(map((response) => response.version));
  }

  updateVariants(id: PatternId, version: number, variants: PatternVariant[]): Observable<number> {
    const request: UpdateVariantsRequest = {
      version,
      variants: variants.map((variant) => this.toApiVariant(variant)),
    };

    return this.http
      .post<UpdateVariantsResponse>(`${environment.apiUrl}/patterns/${id}/update/variants`, request)
      .pipe(map((response) => response.version));
  }

  updateExtras(id: PatternId, version: number, extras: PatternExtra[]): Observable<number> {
    const request: UpdateExtrasRequest = {
      version,
      extras: extras.map((extra) => this.toApiExtra(extra)),
    };

    return this.http
      .post<UpdateExtrasResponse>(`${environment.apiUrl}/patterns/${id}/update/extras`, request)
      .pipe(map((response) => response.version));
  }

  updateDescription(id: PatternId, version: number, description?: string | null): Observable<number> {
    const request: UpdateDescriptionRequest = {
      version,
    };

    someOrNone(description).ifSome((value) => {
      request.description = value;
    });

    return this.http
      .post<UpdateDescriptionResponse>(`${environment.apiUrl}/patterns/${id}/update/description`, request)
      .pipe(map((response) => response.version));
  }

  publishPattern(id: PatternId, version: number): Observable<number> {
    return this.http
      .post<PublishPatternResponse>(
        `${environment.apiUrl}/patterns/${id}/publish`,
        {},
        {
          params: { version: version.toString() },
        },
      )
      .pipe(map((response) => response.version));
  }

  unpublishPattern(id: PatternId, version: number): Observable<number> {
    return this.http
      .post<UnpublishPatternResponse>(
        `${environment.apiUrl}/patterns/${id}/unpublish`,
        {},
        {
          params: { version: version.toString() },
        },
      )
      .pipe(map((response) => response.version));
  }

  featurePattern(id: PatternId, version: number): Observable<number> {
    return this.http
      .post<FeaturePatternResponse>(
        `${environment.apiUrl}/patterns/${id}/feature`,
        {},
        {
          params: { version: version.toString() },
        },
      )
      .pipe(map((response) => response.version));
  }

  unfeaturePattern(id: PatternId, version: number): Observable<number> {
    return this.http
      .post<UnfeaturePatternResponse>(
        `${environment.apiUrl}/patterns/${id}/unfeature`,
        {},
        {
          params: { version: version.toString() },
        },
      )
      .pipe(map((response) => response.version));
  }

  deletePattern(id: string, version: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/patterns/${id}`, {
      params: { version: version.toString() },
    });
  }

  private makeSureEventStreamIsOpen(): void {
    if (this.sse.isNone()) {
      this.openEventStream();
    }
  }

  private openEventStream(): void {
    const token = this.authService.getCurrentToken().orElseThrow();

    const sse = new SSE(`${environment.apiUrl}/patterns/changes`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    sse.onmessage = (event) => {
      const change = JSON.parse(event.data);
      this.events$.next(change);
    };
    sse.onabort = () => this.closeEventStream();
    sse.onerror = () => this.closeEventStream();

    this.sse = some(sse);
  }

  private closeEventStream(): void {
    this.sse.ifSome((sse) => sse.close());
    this.sse = none();
  }

  private toInternalPattern(pattern: PatternDTO): Pattern {
    return Pattern.of({
      id: pattern.id,
      version: pattern.version,
      published: pattern.published,
      featured: pattern.featured,
      name: pattern.name,
      number: pattern.number,
      description: pattern.description,
      attribution: this.toInternalAttribution(pattern.attribution),
      categories: new Set<PatternCategoryId>(pattern.categories),
      images: pattern.images,
      variants: pattern.variants.map((variant) => this.toInternalVariant(variant)),
      extras: pattern.extras.map((extra) => this.toInternalExtra(extra)),
      createdAt: new Date(pattern.createdAt),
    });
  }

  private toInternalAttribution(attribution: PatternAttributionDTO): PatternAttribution {
    return PatternAttribution.of({
      originalPatternName: attribution.originalPatternName,
      designer: attribution.designer,
    });
  }

  private toInternalVariant(variant: PatternVariantDTO): PatternVariant {
    const sizes = variant.pricedSizeRanges.map((range) => this.toInternalPricedSizeRange(range));
    sizes.sort((a, b) => a.from - b.from);

    return PatternVariant.of({
      name: variant.name,
      sizes,
    });
  }

  private toInternalPricedSizeRange(range: PricedSizeRangeDTO): PricedSizeRange {
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

  private toApiAttribution(attribution: PatternAttribution): PatternAttributionDTO {
    const result: PatternAttributionDTO = {};

    attribution.originalPatternName.ifSome((value) => (result.originalPatternName = value));
    attribution.designer.ifSome((value) => (result.designer = value));

    return result;
  }

  private toApiVariant(variant: PatternVariant): PatternVariantDTO {
    return {
      name: variant.name,
      pricedSizeRanges: variant.sizes.map((range) => this.toApiPricedSizeRange(range)),
    };
  }

  private toApiPricedSizeRange(range: PricedSizeRange): PricedSizeRangeDTO {
    return {
      from: range.from,
      to: range.to.orElseNull(),
      unit: range.unit.orElseNull(),
      price: this.toApiMoney(range.price),
    };
  }

  private toApiMoney(price: Money): MoneyDTO {
    return {
      amount: price.value,
      currency: this.toApiCurrency(price.currency),
    };
  }

  private toApiCurrency(currency: Currency): string {
    if (currency.equals(Currency.euro())) {
      return 'EUR';
    }

    throw new Error(`Unknown currency: ${currency}`);
  }

  private toApiExtra(extra: PatternExtra): PatternExtraDTO {
    return {
      name: extra.name,
      price: this.toApiMoney(extra.price),
    };
  }
}
