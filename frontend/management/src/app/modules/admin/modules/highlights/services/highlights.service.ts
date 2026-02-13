import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments';
import { Highlight, HighlightId, Link, LinkType, toLinkType } from '../model';
import { someOrNone } from '@kicherkrabbe/shared';

interface LinkDTO {
  type: string;
  id: string;
  name: string;
}

interface HighlightDTO {
  id: string;
  version: number;
  imageId: string;
  links: LinkDTO[];
  published: boolean;
  sortOrder: number;
  createdAt: string;
}

interface QueryHighlightsResponse {
  skip: number;
  limit: number;
  total: number;
  highlights: HighlightDTO[];
}

interface CreateHighlightRequest {
  imageId: string;
  sortOrder: number;
}

interface CreateHighlightResponse {
  id: string;
}

interface UpdateImageRequest {
  version: number;
  imageId: string;
}

interface UpdateImageResponse {
  version: number;
}

interface AddLinkRequest {
  version: number;
  linkType: string;
  linkId: string;
}

interface AddLinkResponse {
  version: number;
}

interface RemoveLinkRequest {
  version: number;
  linkType: string;
  linkId: string;
}

interface RemoveLinkResponse {
  version: number;
}

interface PublishRequest {
  version: number;
}

interface PublishResponse {
  version: number;
}

interface UnpublishRequest {
  version: number;
}

interface UnpublishResponse {
  version: number;
}

interface UpdateSortOrderRequest {
  version: number;
  sortOrder: number;
}

interface UpdateSortOrderResponse {
  version: number;
}

interface QueryLinksResponse {
  total: number;
  links: LinkDTO[];
}

@Injectable()
export class HighlightsService {
  constructor(private readonly http: HttpClient) {}

  getHighlight(id: HighlightId): Observable<Highlight> {
    return this.http
      .get<HighlightDTO>(`${environment.apiUrl}/highlights/${id}`)
      .pipe(map((highlight) => this.toInternalHighlight(highlight)));
  }

  getHighlights(props: { skip?: number; limit?: number }): Observable<Highlight[]> {
    const params: any = {};
    someOrNone(props.limit).ifSome((limit) => (params['limit'] = limit));
    someOrNone(props.skip).ifSome((skip) => (params['skip'] = skip));

    return this.http
      .get<QueryHighlightsResponse>(`${environment.apiUrl}/highlights`, { params })
      .pipe(map((response) => response.highlights.map((h) => this.toInternalHighlight(h))));
  }

  getLinks(props: { searchTerm?: string; skip?: number; limit?: number }): Observable<Link[]> {
    const params: any = {};
    someOrNone(props.searchTerm).ifSome((searchTerm) => (params['searchTerm'] = searchTerm));
    someOrNone(props.skip).ifSome((skip) => (params['skip'] = skip));
    someOrNone(props.limit).ifSome((limit) => (params['limit'] = limit));

    return this.http
      .get<QueryLinksResponse>(`${environment.apiUrl}/highlights/links`, { params })
      .pipe(map((response) => response.links.map((l) => Link.of({ type: toLinkType(l.type), id: l.id, name: l.name }))));
  }

  createHighlight(props: { imageId: string; sortOrder: number }): Observable<HighlightId> {
    const request: CreateHighlightRequest = {
      imageId: props.imageId,
      sortOrder: props.sortOrder,
    };

    return this.http
      .post<CreateHighlightResponse>(`${environment.apiUrl}/highlights/create`, request)
      .pipe(map((response) => response.id));
  }

  updateImage(id: HighlightId, version: number, imageId: string): Observable<number> {
    const request: UpdateImageRequest = { version, imageId };

    return this.http
      .post<UpdateImageResponse>(`${environment.apiUrl}/highlights/${id}/update/image`, request)
      .pipe(map((response) => response.version));
  }

  addLink(id: HighlightId, version: number, linkType: LinkType, linkId: string): Observable<number> {
    const request: AddLinkRequest = { version, linkType: linkType.internal, linkId };

    return this.http
      .post<AddLinkResponse>(`${environment.apiUrl}/highlights/${id}/links/add`, request)
      .pipe(map((response) => response.version));
  }

  removeLink(id: HighlightId, version: number, linkType: LinkType, linkId: string): Observable<number> {
    const request: RemoveLinkRequest = { version, linkType: linkType.internal, linkId };

    return this.http
      .post<RemoveLinkResponse>(`${environment.apiUrl}/highlights/${id}/links/remove`, request)
      .pipe(map((response) => response.version));
  }

  publishHighlight(id: HighlightId, version: number): Observable<number> {
    const request: PublishRequest = { version };

    return this.http
      .post<PublishResponse>(`${environment.apiUrl}/highlights/${id}/publish`, request)
      .pipe(map((response) => response.version));
  }

  unpublishHighlight(id: HighlightId, version: number): Observable<number> {
    const request: UnpublishRequest = { version };

    return this.http
      .post<UnpublishResponse>(`${environment.apiUrl}/highlights/${id}/unpublish`, request)
      .pipe(map((response) => response.version));
  }

  updateSortOrder(id: HighlightId, version: number, sortOrder: number): Observable<number> {
    const request: UpdateSortOrderRequest = { version, sortOrder };

    return this.http
      .post<UpdateSortOrderResponse>(`${environment.apiUrl}/highlights/${id}/update/sort-order`, request)
      .pipe(map((response) => response.version));
  }

  deleteHighlight(id: HighlightId, version: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/highlights/${id}`, {
      params: { version: version.toString() },
    });
  }

  private toInternalHighlight(dto: HighlightDTO): Highlight {
    return Highlight.of({
      id: dto.id,
      version: dto.version,
      imageId: dto.imageId,
      links: dto.links.map((l) => Link.of({ type: toLinkType(l.type), id: l.id, name: l.name })),
      published: dto.published,
      sortOrder: dto.sortOrder,
      createdAt: new Date(dto.createdAt),
    });
  }
}
