import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import {
  Availability,
  Color,
  Fabric,
  Theme,
  Type,
  TypeAvailability,
} from '../model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments';
import { Image, someOrNone } from '../../../../../util';

interface FabricsAvailabilityFilterDTO {
  active: boolean;
  inStock: boolean;
}

enum FabricsSortPropertyDTO {
  ALPHABETICAL = 'ALPHABETICAL',
}

enum FabricsSortDirectionDTO {
  ASCENDING = 'ASCENDING',
  DESCENDING = 'DESCENDING',
}

interface FabricsSortDTO {
  property: FabricsSortPropertyDTO;
  direction: FabricsSortDirectionDTO;
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

interface FabricTypeAvailabilityDTO {
  typeId: string;
  inStock: boolean;
}

interface PublishedFabricDTO {
  id: string;
  name: string;
  imageId: string;
  colorIds: string[];
  topicIds: string[];
  availability: FabricTypeAvailabilityDTO[];
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

interface QueryPublishedFabricResponse {
  fabric: PublishedFabricDTO;
}

@Injectable()
export class RemoteFabricsService {
  constructor(private readonly http: HttpClient) {}

  getAvailableThemes(): Observable<Theme[]> {
    return this.http
      .get<QueryTopicsResponse>(`${environment.apiUrl}/fabrics/topics/used`)
      .pipe(
        map((response: QueryTopicsResponse) =>
          response.topics.map((topic: TopicDTO) =>
            Theme.of({ id: topic.id, name: topic.name }),
          ),
        ),
      );
  }

  getAvailableColors(): Observable<Color[]> {
    return this.http
      .get<QueryColorsResponse>(`${environment.apiUrl}/fabrics/colors/used`)
      .pipe(
        map((response: QueryColorsResponse) =>
          response.colors.map((color: ColorDTO) =>
            Color.of({
              id: color.id,
              name: color.name,
              hex: `#${color.red.toString(16)}${color.green.toString(16)}${color.blue.toString(16)}`,
            }),
          ),
        ),
      );
  }

  getFabrics(props: {
    topicIds?: string[];
    colorIds?: string[];
    availability?: {
      active: boolean;
      inStock: boolean;
    };
    sort?: {
      ascending: boolean;
    };
    skip?: number;
    limit?: number;
  }): Observable<Fabric[]> {
    const availability = someOrNone(props.availability)
      .map((a) => ({
        active: a.active,
        inStock: a.inStock,
      }))
      .orElse({
        active: false,
        inStock: true,
      });
    const sort = someOrNone(props.sort)
      .map((s) => ({
        property: FabricsSortPropertyDTO.ALPHABETICAL,
        direction: s.ascending
          ? FabricsSortDirectionDTO.ASCENDING
          : FabricsSortDirectionDTO.DESCENDING,
      }))
      .orElse({
        property: FabricsSortPropertyDTO.ALPHABETICAL,
        direction: FabricsSortDirectionDTO.ASCENDING,
      });

    const request: QueryPublishedFabricsRequest = {
      searchTerm: '',
      colorIds: props.colorIds ?? [],
      topicIds: props.topicIds ?? [],
      availability,
      sort,
      skip: props.skip ?? 0,
      limit: props.limit ?? 100,
    };

    return this.http
      .post<QueryPublishedFabricsResponse>(
        `${environment.apiUrl}/fabrics/published`,
        request,
      )
      .pipe(
        map((response: QueryPublishedFabricsResponse) =>
          response.fabrics.map((fabric: PublishedFabricDTO) =>
            this.mapPublishedFabricDTOToFabric(fabric),
          ),
        ),
      );
  }

  getFabric(id: string): Observable<Fabric> {
    return this.http
      .get<QueryPublishedFabricResponse>(
        `${environment.apiUrl}/fabrics/${id}/published`,
      )
      .pipe(
        map((response: QueryPublishedFabricResponse) =>
          this.mapPublishedFabricDTOToFabric(response.fabric),
        ),
      );
  }

  private mapPublishedFabricDTOToFabric(fabric: PublishedFabricDTO): Fabric {
    return Fabric.of({
      id: fabric.id,
      name: fabric.name,
      image: Image.of({
        url: `${environment.apiUrl}/assets/${fabric.imageId}/content`,
      }),
      colors: new Set(fabric.colorIds),
      themes: new Set(fabric.topicIds),
      availability: Availability.of({
        types: fabric.availability.map((availability) =>
          TypeAvailability.of({
            type: Type.of({ id: availability.typeId, name: 'Test' }),
            inStock: availability.inStock,
          }),
        ),
      }),
    });
  }
}
