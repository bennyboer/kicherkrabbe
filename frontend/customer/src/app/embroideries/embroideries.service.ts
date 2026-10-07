import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable, shareReplay } from "rxjs";
import { none, type Option } from "@kicherkrabbe/shared";
import { environment } from "../../environments";
import { Category } from "./model";
import { Embroidery } from "./embroidery";

interface PublishedEmbroideryDTO {
	id: string;
	alias: string;
	name: string;
	image: string;
	categories: string[];
}

interface EmbroideriesSortDTO {
	property: "ALPHABETICAL";
	direction: "ASCENDING" | "DESCENDING";
}

interface QueryPublishedEmbroideriesRequest {
	searchTerm: string;
	categories: string[];
	sort: EmbroideriesSortDTO;
	skip: number;
	limit: number;
}

interface QueryPublishedEmbroideriesResponse {
	skip: number;
	limit: number;
	total: number;
	embroideries: PublishedEmbroideryDTO[];
}

interface QueryPublishedEmbroideryResponse {
	embroidery: PublishedEmbroideryDTO;
}

interface QueryFeaturedEmbroideriesResponse {
	embroideries: PublishedEmbroideryDTO[];
}

interface CategoryDTO {
	id: string;
	name: string;
}

interface QueryCategoriesResponse {
	categories: CategoryDTO[];
}

export interface EmbroideriesQueryResult {
	embroideries: Embroidery[];
	total: number;
	skip: number;
	limit: number;
}

@Injectable({
	providedIn: "root",
})
export class EmbroideriesService {
	private categoriesCache$: Observable<Category[]> | null = null;

	constructor(private readonly http: HttpClient) {}

	getEmbroideries(props: {
		searchTerm?: string;
		categoryIds?: string[];
		ascending?: boolean;
		skip?: number;
		limit?: number;
	}): Observable<EmbroideriesQueryResult> {
		const request: QueryPublishedEmbroideriesRequest = {
			searchTerm: props.searchTerm ?? "",
			categories: props.categoryIds ?? [],
			sort: {
				property: "ALPHABETICAL",
				direction: (props.ascending ?? true) ? "ASCENDING" : "DESCENDING",
			},
			skip: props.skip ?? 0,
			limit: props.limit ?? 50,
		};

		return this.http
			.post<QueryPublishedEmbroideriesResponse>(
				`${environment.apiUrl}/embroideries/published`,
				request,
			)
			.pipe(
				map((response) => ({
					embroideries: response.embroideries.map((e) =>
						this.toInternalEmbroidery(e),
					),
					total: response.total,
					skip: response.skip,
					limit: response.limit,
				})),
			);
	}

	getEmbroidery(idOrAlias: string): Observable<Embroidery> {
		return this.http
			.get<QueryPublishedEmbroideryResponse>(
				`${environment.apiUrl}/embroideries/${idOrAlias}/published`,
			)
			.pipe(map((response) => this.toInternalEmbroidery(response.embroidery)));
	}

	getFeaturedEmbroideries(seed: Option<number> = none()): Observable<Embroidery[]> {
		let url = `${environment.apiUrl}/embroideries/featured`;
		seed.ifSome((s) => (url += `?seed=${s}`));

		return this.http
			.get<QueryFeaturedEmbroideriesResponse>(url)
			.pipe(
				map((response) =>
					response.embroideries.map((e) => this.toInternalEmbroidery(e)),
				),
			);
	}

	getAvailableCategories(): Observable<Category[]> {
		if (!this.categoriesCache$) {
			this.categoriesCache$ = this.http
				.get<QueryCategoriesResponse>(
					`${environment.apiUrl}/embroideries/categories/used`,
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

	getImageUrl(imageId: string, width?: number): string {
		const baseUrl = `${environment.apiUrl}/assets/${imageId}/content`;
		return width ? `${baseUrl}?width=${width}` : baseUrl;
	}

	private toInternalEmbroidery(dto: PublishedEmbroideryDTO): Embroidery {
		return Embroidery.of({
			id: dto.id,
			alias: dto.alias,
			name: dto.name,
			image: dto.image,
			categoryIds: dto.categories ?? [],
		});
	}
}
