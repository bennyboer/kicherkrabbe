import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable, shareReplay } from "rxjs";
import { environment } from "../../environments";
import { none, Option } from "@kicherkrabbe/shared";
import { Fabric, TypeAvailability } from "./fabric";
import { Color, FabricType, Topic } from "./model";

interface FabricTypeAvailabilityDTO {
	typeId: string;
	inStock: boolean;
}

interface PublishedFabricDTO {
	id: string;
	alias: string;
	name: string;
	imageId: string;
	colorIds: string[];
	topicIds: string[];
	availability: FabricTypeAvailabilityDTO[];
}

interface QueryFeaturedFabricsResponse {
	fabrics: PublishedFabricDTO[];
}

interface FabricsAvailabilityFilterDTO {
	active: boolean;
	inStock: boolean;
}

interface FabricsSortDTO {
	property: "ALPHABETICAL";
	direction: "ASCENDING" | "DESCENDING";
}

interface QueryPublishedFabricsRequest {
	searchTerm: string;
	colorIds: string[];
	topicIds: string[];
	availability: FabricsAvailabilityFilterDTO;
	sort: FabricsSortDTO;
	skip: number;
	limit: number;
}

interface QueryPublishedFabricsResponse {
	skip: number;
	limit: number;
	total: number;
	fabrics: PublishedFabricDTO[];
}

interface TopicDTO {
	id: string;
	name: string;
}

interface QueryTopicsResponse {
	topics: TopicDTO[];
}

interface ColorDTO {
	id: string;
	name: string;
	red: number;
	green: number;
	blue: number;
}

interface QueryColorsResponse {
	colors: ColorDTO[];
}

interface FabricTypeDTO {
	id: string;
	name: string;
}

interface QueryFabricTypesResponse {
	fabricTypes: FabricTypeDTO[];
}

interface QueryPublishedFabricResponse {
	fabric: PublishedFabricDTO;
}

export interface FabricsQueryResult {
	fabrics: Fabric[];
	total: number;
	skip: number;
	limit: number;
}

@Injectable({
	providedIn: "root",
})
export class FabricsService {
	private topicsCache$: Observable<Topic[]> | null = null;
	private colorsCache$: Observable<Color[]> | null = null;
	private fabricTypesCache$: Observable<FabricType[]> | null = null;

	constructor(private readonly http: HttpClient) {}

	getFeaturedFabrics(seed: Option<number> = none()): Observable<Fabric[]> {
		let url = `${environment.apiUrl}/fabrics/featured`;
		seed.ifSome((s) => (url += `?seed=${s}`));

		return this.http.get<QueryFeaturedFabricsResponse>(url).pipe(
			map((response) =>
				response.fabrics.map((fabric) => this.toInternalFabric(fabric))
			)
		);
	}

	getFabrics(props: {
		topicIds?: string[];
		colorIds?: string[];
		inStockOnly?: boolean;
		sortAscending?: boolean;
		skip?: number;
		limit?: number;
	}): Observable<FabricsQueryResult> {
		const request: QueryPublishedFabricsRequest = {
			searchTerm: "",
			colorIds: props.colorIds ?? [],
			topicIds: props.topicIds ?? [],
			availability: {
				active: props.inStockOnly === true,
				inStock: true,
			},
			sort: {
				property: "ALPHABETICAL",
				direction: props.sortAscending !== false ? "ASCENDING" : "DESCENDING",
			},
			skip: props.skip ?? 0,
			limit: props.limit ?? 50,
		};

		return this.http
			.post<QueryPublishedFabricsResponse>(
				`${environment.apiUrl}/fabrics/published`,
				request
			)
			.pipe(
				map((response) => ({
					fabrics: response.fabrics.map((f) => this.toInternalFabric(f)),
					total: response.total,
					skip: response.skip,
					limit: response.limit,
				}))
			);
	}

	getFabric(id: string): Observable<Fabric> {
		return this.http
			.get<QueryPublishedFabricResponse>(
				`${environment.apiUrl}/fabrics/${id}/published`
			)
			.pipe(map((response) => this.toInternalFabric(response.fabric)));
	}

	getAvailableTopics(): Observable<Topic[]> {
		if (!this.topicsCache$) {
			this.topicsCache$ = this.http
				.get<QueryTopicsResponse>(`${environment.apiUrl}/fabrics/topics/used`)
				.pipe(
					map((response) =>
						response.topics.map((topic) =>
							Topic.of({ id: topic.id, name: topic.name })
						)
					),
					shareReplay(1)
				);
		}
		return this.topicsCache$;
	}

	getAvailableColors(): Observable<Color[]> {
		if (!this.colorsCache$) {
			this.colorsCache$ = this.http
				.get<QueryColorsResponse>(`${environment.apiUrl}/fabrics/colors/used`)
				.pipe(
					map((response) =>
						response.colors.map((color) =>
							Color.of({
								id: color.id,
								name: color.name,
								red: color.red,
								green: color.green,
								blue: color.blue,
							})
						)
					),
					shareReplay(1)
				);
		}
		return this.colorsCache$;
	}

	getAvailableFabricTypes(): Observable<FabricType[]> {
		if (!this.fabricTypesCache$) {
			this.fabricTypesCache$ = this.http
				.get<QueryFabricTypesResponse>(
					`${environment.apiUrl}/fabrics/fabric-types/used`
				)
				.pipe(
					map((response) =>
						response.fabricTypes.map((ft) =>
							FabricType.of({ id: ft.id, name: ft.name })
						)
					),
					shareReplay(1)
				);
		}
		return this.fabricTypesCache$;
	}

	getImageUrl(imageId: string, width?: number): string {
		const baseUrl = `${environment.apiUrl}/assets/${imageId}/content`;
		return width ? `${baseUrl}?width=${width}` : baseUrl;
	}

	private toInternalFabric(dto: PublishedFabricDTO): Fabric {
		return Fabric.of({
			id: dto.id,
			alias: dto.alias,
			name: dto.name,
			imageId: dto.imageId,
			colorIds: dto.colorIds ?? [],
			topicIds: dto.topicIds ?? [],
			availability: (dto.availability ?? []).map(
				(a): TypeAvailability => ({
					typeId: a.typeId,
					inStock: a.inStock,
				})
			),
		});
	}
}
