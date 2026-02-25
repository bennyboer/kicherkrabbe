import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable, shareReplay } from "rxjs";
import { environment } from "../../environments";
import { Category, FabricCompositionItem, Money, Notes, OfferLink, Pricing } from "./model";
import { Offer } from "./offer";

interface MoneyDTO {
	amount: number;
	currency: string;
}

interface PricingDTO {
	price: MoneyDTO;
	discountedPrice: MoneyDTO | null;
}

interface NotesDTO {
	description: string;
	contains: string | null;
	care: string | null;
	safety: string | null;
}

interface LinkDTO {
	type: string;
	id: string;
	name: string;
}

interface FabricCompositionItemDTO {
	fabricType: string;
	percentage: number;
}

interface PublishedOfferDTO {
	id: string;
	alias: string;
	title: string;
	size: string;
	categoryIds: string[];
	imageIds: string[];
	links: LinkDTO[];
	fabricCompositionItems: FabricCompositionItemDTO[];
	pricing: PricingDTO;
	notes: NotesDTO;
}

interface OffersSortDTO {
	property: "NEWEST" | "ALPHABETICAL" | "PRICE";
	direction: "ASCENDING" | "DESCENDING";
}

interface PriceRangeDTO {
	minPrice: number | null;
	maxPrice: number | null;
}

interface QueryPublishedOffersRequest {
	searchTerm: string;
	categories: string[];
	sizes: string[];
	priceRange: PriceRangeDTO | null;
	sort: OffersSortDTO;
	skip: number;
	limit: number;
}

interface QueryPublishedOffersResponse {
	skip: number;
	limit: number;
	total: number;
	offers: PublishedOfferDTO[];
}

interface QueryPublishedOfferResponse {
	offer: PublishedOfferDTO;
}

interface CategoryDTO {
	id: string;
	name: string;
}

interface QueryCategoriesResponse {
	categories: CategoryDTO[];
}

interface QuerySizesResponse {
	sizes: string[];
}

export interface OffersQueryResult {
	offers: Offer[];
	total: number;
	skip: number;
	limit: number;
}

@Injectable({
	providedIn: "root",
})
export class OffersService {
	private categoriesCache$: Observable<Category[]> | null = null;
	private sizesCache$: Observable<string[]> | null = null;

	constructor(private readonly http: HttpClient) {}

	getOffers(props: {
		searchTerm?: string;
		categoryIds?: string[];
		sizes?: string[];
		priceRange?: { minPrice: number | null; maxPrice: number | null } | null;
		sort?: { property: "NEWEST" | "ALPHABETICAL" | "PRICE"; direction: "ASCENDING" | "DESCENDING" };
		skip?: number;
		limit?: number;
	}): Observable<OffersQueryResult> {
		const request: QueryPublishedOffersRequest = {
			searchTerm: props.searchTerm ?? "",
			categories: props.categoryIds ?? [],
			sizes: props.sizes ?? [],
			priceRange: props.priceRange ?? null,
			sort: props.sort ?? { property: "NEWEST", direction: "DESCENDING" },
			skip: props.skip ?? 0,
			limit: props.limit ?? 50,
		};

		return this.http
			.post<QueryPublishedOffersResponse>(
				`${environment.apiUrl}/offers/published`,
				request,
			)
			.pipe(
				map((response) => ({
					offers: response.offers.map((o) => this.toInternalOffer(o)),
					total: response.total,
					skip: response.skip,
					limit: response.limit,
				})),
			);
	}

	getOffer(id: string): Observable<Offer> {
		return this.http
			.get<QueryPublishedOfferResponse>(
				`${environment.apiUrl}/offers/${id}/published`,
			)
			.pipe(map((response) => this.toInternalOffer(response.offer)));
	}

	getAvailableCategories(): Observable<Category[]> {
		if (!this.categoriesCache$) {
			this.categoriesCache$ = this.http
				.get<QueryCategoriesResponse>(
					`${environment.apiUrl}/offers/categories`,
				)
				.pipe(
					map((response) =>
						response.categories.map((category) =>
							Category.of({ id: category.id, name: category.name }),
						),
					),
					shareReplay(1),
				);
		}
		return this.categoriesCache$;
	}

	getAvailableSizes(): Observable<string[]> {
		if (!this.sizesCache$) {
			this.sizesCache$ = this.http
				.get<QuerySizesResponse>(
					`${environment.apiUrl}/offers/sizes`,
				)
				.pipe(
					map((response) => response.sizes),
					shareReplay(1),
				);
		}
		return this.sizesCache$;
	}

	getImageUrl(imageId: string, width?: number): string {
		const baseUrl = `${environment.apiUrl}/assets/${imageId}/content`;
		return width ? `${baseUrl}?width=${width}` : baseUrl;
	}

	private toInternalOffer(dto: PublishedOfferDTO): Offer {
		return Offer.of({
			id: dto.id,
			alias: dto.alias,
			title: dto.title,
			size: dto.size,
			categoryIds: dto.categoryIds ?? [],
			images: dto.imageIds ?? [],
			links: (dto.links ?? []).map((l) => this.toInternalLink(l)),
			fabricCompositionItems: (dto.fabricCompositionItems ?? []).map((f) => this.toInternalFabricCompositionItem(f)),
			pricing: this.toInternalPricing(dto.pricing),
			notes: this.toInternalNotes(dto.notes),
		});
	}

	private toInternalLink(dto: LinkDTO): OfferLink {
		return OfferLink.of({ type: dto.type, id: dto.id, name: dto.name });
	}

	private toInternalFabricCompositionItem(dto: FabricCompositionItemDTO): FabricCompositionItem {
		return FabricCompositionItem.of({ fabricType: dto.fabricType, percentage: dto.percentage });
	}

	private toInternalPricing(dto: PricingDTO) {
		return Pricing.of({
			price: Money.of({ amount: dto.price.amount, currency: dto.price.currency }),
			discountedPrice: dto.discountedPrice
				? Money.of({ amount: dto.discountedPrice.amount, currency: dto.discountedPrice.currency })
				: null,
		});
	}

	private toInternalNotes(dto: NotesDTO) {
		return Notes.of({
			description: dto.description,
			contains: dto.contains,
			care: dto.care,
			safety: dto.safety,
		});
	}
}
