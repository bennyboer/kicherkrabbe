import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable, shareReplay } from "rxjs";
import { environment } from "../../environments";
import { none, Option } from "@kicherkrabbe/shared";
import {
	Pattern,
	PatternAttribution,
	PatternExtra,
	PatternVariant,
	PricedSizeRange,
} from "./pattern";
import { Category } from "./model";

interface MoneyDTO {
	amount: number;
	currency: string;
}

interface PricedSizeRangeDTO {
	from: number;
	to: number | null;
	unit: string | null;
	price: MoneyDTO;
}

interface PatternVariantDTO {
	name: string;
	pricedSizeRanges: PricedSizeRangeDTO[];
}

interface PatternExtraDTO {
	name: string;
	price: MoneyDTO;
}

interface PatternAttributionDTO {
	originalPatternName?: string;
	designer?: string;
}

interface PublishedPatternDTO {
	id: string;
	alias: string;
	name: string;
	number?: string;
	description?: string;
	attribution?: PatternAttributionDTO;
	images: string[];
	variants: PatternVariantDTO[];
	extras?: PatternExtraDTO[];
	categories: string[];
}

interface QueryFeaturedPatternsResponse {
	patterns: PublishedPatternDTO[];
}

interface QueryPublishedPatternResponse {
	pattern: PublishedPatternDTO;
}

interface PatternsSortDTO {
	property: "ALPHABETICAL";
	direction: "ASCENDING" | "DESCENDING";
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

interface CategoryDTO {
	id: string;
	name: string;
}

interface QueryCategoriesResponse {
	categories: CategoryDTO[];
}

export interface PatternsQueryResult {
	patterns: Pattern[];
	total: number;
	skip: number;
	limit: number;
}

export interface SizeOption {
	value: number;
	label: string;
}

@Injectable({
	providedIn: "root",
})
export class PatternsService {
	private categoriesCache$: Observable<Category[]> | null = null;

	constructor(private readonly http: HttpClient) {}

	getFeaturedPatterns(seed: Option<number> = none()): Observable<Pattern[]> {
		let url = `${environment.apiUrl}/patterns/featured`;
		seed.ifSome((s) => (url += `?seed=${s}`));

		return this.http.get<QueryFeaturedPatternsResponse>(url).pipe(
			map((response) =>
				response.patterns.map((pattern) => this.toInternalPattern(pattern))
			)
		);
	}

	getPatterns(props: {
		categoryIds?: string[];
		sizes?: number[];
		sortAscending?: boolean;
		skip?: number;
		limit?: number;
	}): Observable<PatternsQueryResult> {
		const request: QueryPublishedPatternsRequest = {
			searchTerm: "",
			categories: props.categoryIds ?? [],
			sizes: props.sizes ?? [],
			sort: {
				property: "ALPHABETICAL",
				direction: props.sortAscending !== false ? "ASCENDING" : "DESCENDING",
			},
			skip: props.skip ?? 0,
			limit: props.limit ?? 50,
		};

		return this.http
			.post<QueryPublishedPatternsResponse>(
				`${environment.apiUrl}/patterns/published`,
				request
			)
			.pipe(
				map((response) => ({
					patterns: response.patterns.map((p) => this.toInternalPattern(p)),
					total: response.total,
					skip: response.skip,
					limit: response.limit,
				}))
			);
	}

	getPattern(id: string): Observable<Pattern> {
		return this.http
			.get<QueryPublishedPatternResponse>(
				`${environment.apiUrl}/patterns/${id}/published`
			)
			.pipe(map((response) => this.toInternalPattern(response.pattern)));
	}

	getAvailableCategories(): Observable<Category[]> {
		if (!this.categoriesCache$) {
			this.categoriesCache$ = this.http
				.get<QueryCategoriesResponse>(
					`${environment.apiUrl}/patterns/categories/used`
				)
				.pipe(
					map((response) =>
						response.categories.map((category) =>
							Category.of({ id: category.id, name: category.name })
						)
					),
					shareReplay(1)
				);
		}
		return this.categoriesCache$;
	}

	getAvailableSizes(): SizeOption[] {
		const sizes: SizeOption[] = [];
		for (let size = 50; size <= 116; size += 6) {
			sizes.push({ value: size, label: `${size}` });
		}
		return sizes;
	}

	getImageUrl(imageId: string): string {
		return `${environment.apiUrl}/assets/${imageId}/content`;
	}

	private toInternalPattern(dto: PublishedPatternDTO): Pattern {
		return Pattern.of({
			id: dto.id,
			alias: dto.alias,
			name: dto.name,
			number: dto.number ?? null,
			description: dto.description ?? null,
			attribution: this.toInternalAttribution(dto.attribution),
			images: dto.images,
			variants: dto.variants.map((v) => this.toInternalVariant(v)),
			extras: (dto.extras ?? []).map((e) => this.toInternalExtra(e)),
			categoryIds: dto.categories ?? [],
		});
	}

	private toInternalAttribution(
		dto?: PatternAttributionDTO
	): PatternAttribution | null {
		if (!dto) {
			return null;
		}
		return {
			originalPatternName: dto.originalPatternName ?? null,
			designer: dto.designer ?? null,
		};
	}

	private toInternalVariant(dto: PatternVariantDTO): PatternVariant {
		return {
			name: dto.name,
			pricedSizeRanges: dto.pricedSizeRanges.map((r) =>
				this.toInternalPricedSizeRange(r)
			),
		};
	}

	private toInternalExtra(dto: PatternExtraDTO): PatternExtra {
		return {
			name: dto.name,
			price: {
				amount: dto.price.amount,
				currency: dto.price.currency,
			},
		};
	}

	private toInternalPricedSizeRange(dto: PricedSizeRangeDTO): PricedSizeRange {
		return {
			from: dto.from,
			to: dto.to,
			unit: dto.unit,
			price: {
				amount: dto.price.amount,
				currency: dto.price.currency,
			},
		};
	}
}
