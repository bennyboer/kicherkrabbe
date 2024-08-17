import { Injectable, OnDestroy } from '@angular/core';
import { filter, map, Observable, Subject, takeUntil } from 'rxjs';
import {
  Currency,
  Money,
  none,
  Option,
  some,
  someOrNone,
} from '../../../../../util';
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

interface PatternDTO {
  id: string;
  version: number;
  published: boolean;
  name: string;
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

interface UpdateImagesRequest {
  version: number;
  images: string[];
}

interface UpdateAttributionRequest {
  version: number;
  attribution: PatternAttributionDTO;
}

interface UpdateCategoriesRequest {
  version: number;
  categories: string[];
}

interface UpdateVariantsRequest {
  version: number;
  variants: PatternVariantDTO[];
}

interface UpdateExtrasRequest {
  version: number;
  extras: PatternExtraDTO[];
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
  private readonly events$: Subject<PatternChangeDTO> =
    new Subject<PatternChangeDTO>();
  private readonly destroy$: Subject<void> = new Subject<void>();

  private sse: Option<SSE> = none();

  constructor(
    private readonly http: HttpClient,
    private readonly authService: AdminAuthService,
  ) {
    const loggedOut$ = this.authService
      .getToken()
      .pipe(filter((token) => token.isNone()));
    loggedOut$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.closeEventStream());
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
      .pipe(
        map((response) =>
          response.patterns.map((p) => this.toInternalPattern(p)),
        ),
      );
  }

  createPattern(props: {
    name: string;
    attribution: PatternAttribution;
    categories: PatternCategoryId[];
    images: string[];
    variants: PatternVariant[];
    extras: PatternExtra[];
  }): Observable<PatternId> {
    const request: CreatePatternRequest = {
      name: props.name,
      attribution: this.toApiAttribution(props.attribution),
      categories: props.categories,
      images: props.images,
      variants: props.variants.map((variant) => this.toApiVariant(variant)),
      extras: props.extras.map((extra) => this.toApiExtra(extra)),
    };

    return this.http
      .post<CreatePatternResponse>(
        `${environment.apiUrl}/patterns/create`,
        request,
      )
      .pipe(map((response) => response.id));
  }

  renamePattern(
    id: PatternId,
    version: number,
    name: string,
  ): Observable<void> {
    const request: RenamePatternRequest = { version, name };

    return this.http.post<void>(
      `${environment.apiUrl}/patterns/${id}/rename`,
      request,
    );
  }

  updateImages(
    id: PatternId,
    version: number,
    images: string[],
  ): Observable<void> {
    const request: UpdateImagesRequest = { version, images };

    return this.http.post<void>(
      `${environment.apiUrl}/patterns/${id}/update/images`,
      request,
    );
  }

  updateAttribution(
    id: PatternId,
    version: number,
    attribution: PatternAttribution,
  ): Observable<void> {
    const request: UpdateAttributionRequest = {
      version,
      attribution: this.toApiAttribution(attribution),
    };

    return this.http.post<void>(
      `${environment.apiUrl}/patterns/${id}/update/attribution`,
      request,
    );
  }

  updateCategories(
    id: PatternId,
    version: number,
    categories: PatternCategoryId[],
  ): Observable<void> {
    const request: UpdateCategoriesRequest = { version, categories };

    return this.http.post<void>(
      `${environment.apiUrl}/patterns/${id}/update/categories`,
      request,
    );
  }

  updateVariants(
    id: PatternId,
    version: number,
    variants: PatternVariant[],
  ): Observable<void> {
    const request: UpdateVariantsRequest = {
      version,
      variants: variants.map((variant) => this.toApiVariant(variant)),
    };

    return this.http.post<void>(
      `${environment.apiUrl}/patterns/${id}/update/variants`,
      request,
    );
  }

  updateExtras(
    id: PatternId,
    version: number,
    extras: PatternExtra[],
  ): Observable<void> {
    const request: UpdateExtrasRequest = {
      version,
      extras: extras.map((extra) => this.toApiExtra(extra)),
    };

    return this.http.post<void>(
      `${environment.apiUrl}/patterns/${id}/update/extras`,
      request,
    );
  }

  publishPattern(id: PatternId, version: number): Observable<void> {
    return this.http.post<void>(
      `${environment.apiUrl}/patterns/${id}/publish`,
      {},
      {
        params: { version: version.toString() },
      },
    );
  }

  unpublishPattern(id: PatternId, version: number): Observable<void> {
    return this.http.post<void>(
      `${environment.apiUrl}/patterns/${id}/unpublish`,
      {},
      {
        params: { version: version.toString() },
      },
    );
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
      name: pattern.name,
      attribution: this.toInternalAttribution(pattern.attribution),
      categories: new Set<PatternCategoryId>(pattern.categories),
      images: pattern.images,
      variants: pattern.variants.map((variant) =>
        this.toInternalVariant(variant),
      ),
      extras: pattern.extras.map((extra) => this.toInternalExtra(extra)),
      createdAt: new Date(pattern.createdAt),
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

  private toApiAttribution(
    attribution: PatternAttribution,
  ): PatternAttributionDTO {
    const result: PatternAttributionDTO = {};

    attribution.originalPatternName.ifSome(
      (value) => (result.originalPatternName = value),
    );
    attribution.designer.ifSome((value) => (result.designer = value));

    return result;
  }

  private toApiVariant(variant: PatternVariant): PatternVariantDTO {
    return {
      name: variant.name,
      pricedSizeRanges: variant.sizes.map((range) =>
        this.toApiPricedSizeRange(range),
      ),
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
