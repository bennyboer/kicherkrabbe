import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { environment } from "../../environments";
import { Fabric } from "./fabric";

interface PublishedFabricDTO {
	id: string;
	name: string;
	imageId: string;
}

interface QueryFeaturedFabricsResponse {
	fabrics: PublishedFabricDTO[];
}

@Injectable({
	providedIn: "root",
})
export class FabricsService {
	constructor(private readonly http: HttpClient) {}

	getFeaturedFabrics(): Observable<Fabric[]> {
		return this.http
			.get<QueryFeaturedFabricsResponse>(
				`${environment.apiUrl}/fabrics/featured`
			)
			.pipe(
				map((response) =>
					response.fabrics.map((fabric) => this.toInternalFabric(fabric))
				)
			);
	}

	getImageUrl(imageId: string): string {
		return `${environment.apiUrl}/assets/${imageId}/content`;
	}

	private toInternalFabric(dto: PublishedFabricDTO): Fabric {
		return Fabric.of({
			id: dto.id,
			name: dto.name,
			imageId: dto.imageId,
		});
	}
}
