import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { environment } from "../../environments";
import { none, Option } from "../util/option";
import { Pattern, PatternVariant, PricedSizeRange } from "./pattern";

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

interface PublishedPatternDTO {
	id: string;
	name: string;
	images: string[];
	variants: PatternVariantDTO[];
}

interface QueryFeaturedPatternsResponse {
	patterns: PublishedPatternDTO[];
}

@Injectable({
	providedIn: "root",
})
export class PatternsService {
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

	getImageUrl(imageId: string): string {
		return `${environment.apiUrl}/assets/${imageId}/content`;
	}

	private toInternalPattern(dto: PublishedPatternDTO): Pattern {
		return Pattern.of({
			id: dto.id,
			name: dto.name,
			images: dto.images,
			variants: dto.variants.map((v) => this.toInternalVariant(v)),
		});
	}

	private toInternalVariant(dto: PatternVariantDTO): PatternVariant {
		return {
			name: dto.name,
			pricedSizeRanges: dto.pricedSizeRanges.map((r) =>
				this.toInternalPricedSizeRange(r)
			),
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
