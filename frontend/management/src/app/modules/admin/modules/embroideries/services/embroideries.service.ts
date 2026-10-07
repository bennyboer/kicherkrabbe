import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments';
import { Embroidery, EmbroideryCategoryId, EmbroideryId, ImageId } from '../model';
import { someOrNone } from '@kicherkrabbe/shared';

interface EmbroideryDTO {
  id: string;
  version: number;
  published: boolean;
  featured: boolean;
  name: string;
  image: string;
  categories: string[];
  createdAt: string;
}

interface QueryEmbroideryResponse {
  embroidery: EmbroideryDTO;
}

interface QueryEmbroideriesRequest {
  searchTerm: string;
  categories: string[];
  skip: number;
  limit: number;
}

interface QueryEmbroideriesResponse {
  skip: number;
  limit: number;
  total: number;
  embroideries: EmbroideryDTO[];
}

interface CreateEmbroideryRequest {
  name: string;
  image: string;
  categories: string[];
}

interface CreateEmbroideryResponse {
  id: string;
}

interface RenameEmbroideryRequest {
  version: number;
  name: string;
}

interface UpdateImageRequest {
  version: number;
  image: string;
}

interface UpdateCategoriesRequest {
  version: number;
  categories: string[];
}

interface VersionResponse {
  version: number;
}

@Injectable()
export class EmbroideriesService {
  constructor(private readonly http: HttpClient) {}

  getEmbroidery(id: EmbroideryId): Observable<Embroidery> {
    return this.http
      .get<QueryEmbroideryResponse>(`${environment.apiUrl}/embroideries/${id}`)
      .pipe(map((response) => this.toInternalEmbroidery(response.embroidery)));
  }

  getEmbroideries(props: {
    searchTerm?: string;
    categories?: string[];
    skip?: number;
    limit?: number;
  }): Observable<{ total: number; embroideries: Embroidery[] }> {
    const request: QueryEmbroideriesRequest = {
      searchTerm: someOrNone(props.searchTerm).orElse(''),
      categories: someOrNone(props.categories).orElse([]),
      skip: someOrNone(props.skip).orElse(0),
      limit: someOrNone(props.limit).orElse(100),
    };

    return this.http.post<QueryEmbroideriesResponse>(`${environment.apiUrl}/embroideries`, request).pipe(
      map((response) => ({
        total: response.total,
        embroideries: response.embroideries.map((e) => this.toInternalEmbroidery(e)),
      })),
    );
  }

  createEmbroidery(props: {
    name: string;
    image: ImageId;
    categories: EmbroideryCategoryId[];
  }): Observable<EmbroideryId> {
    const request: CreateEmbroideryRequest = {
      name: props.name,
      image: props.image,
      categories: props.categories,
    };

    return this.http
      .post<CreateEmbroideryResponse>(`${environment.apiUrl}/embroideries/create`, request)
      .pipe(map((response) => response.id));
  }

  renameEmbroidery(id: EmbroideryId, version: number, name: string): Observable<number> {
    const request: RenameEmbroideryRequest = { version, name };

    return this.http
      .post<VersionResponse>(`${environment.apiUrl}/embroideries/${id}/rename`, request)
      .pipe(map((response) => response.version));
  }

  updateImage(id: EmbroideryId, version: number, image: ImageId): Observable<number> {
    const request: UpdateImageRequest = { version, image };

    return this.http
      .post<VersionResponse>(`${environment.apiUrl}/embroideries/${id}/update/image`, request)
      .pipe(map((response) => response.version));
  }

  updateCategories(id: EmbroideryId, version: number, categories: EmbroideryCategoryId[]): Observable<number> {
    const request: UpdateCategoriesRequest = { version, categories };

    return this.http
      .post<VersionResponse>(`${environment.apiUrl}/embroideries/${id}/update/categories`, request)
      .pipe(map((response) => response.version));
  }

  publishEmbroidery(id: EmbroideryId, version: number): Observable<number> {
    return this.postVersionedAction(id, version, 'publish');
  }

  unpublishEmbroidery(id: EmbroideryId, version: number): Observable<number> {
    return this.postVersionedAction(id, version, 'unpublish');
  }

  featureEmbroidery(id: EmbroideryId, version: number): Observable<number> {
    return this.postVersionedAction(id, version, 'feature');
  }

  unfeatureEmbroidery(id: EmbroideryId, version: number): Observable<number> {
    return this.postVersionedAction(id, version, 'unfeature');
  }

  deleteEmbroidery(id: EmbroideryId, version: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/embroideries/${id}`, {
      params: { version: version.toString() },
    });
  }

  private postVersionedAction(id: EmbroideryId, version: number, action: string): Observable<number> {
    return this.http
      .post<VersionResponse>(
        `${environment.apiUrl}/embroideries/${id}/${action}`,
        {},
        {
          params: { version: version.toString() },
        },
      )
      .pipe(map((response) => response.version));
  }

  private toInternalEmbroidery(embroidery: EmbroideryDTO): Embroidery {
    return Embroidery.of({
      id: embroidery.id,
      version: embroidery.version,
      published: embroidery.published,
      featured: embroidery.featured,
      name: embroidery.name,
      image: embroidery.image,
      categories: new Set<EmbroideryCategoryId>(embroidery.categories),
      createdAt: new Date(embroidery.createdAt),
    });
  }
}
