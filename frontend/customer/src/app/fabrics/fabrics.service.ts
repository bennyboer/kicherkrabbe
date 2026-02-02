import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { environment } from "../../environments";
import { none, Option } from "../util/option";
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

	getFeaturedFabrics(seed: Option<number> = none()): Observable<Fabric[]> {
		let url = `${environment.apiUrl}/fabrics/featured`;
		seed.ifSome((s) => (url += `?seed=${s}`));

		return this.http.get<QueryFeaturedFabricsResponse>(url).pipe(
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
