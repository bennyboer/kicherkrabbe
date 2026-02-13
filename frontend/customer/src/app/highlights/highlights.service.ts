import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { validateProps } from '@kicherkrabbe/shared';
import { environment } from '../../environments';

interface QueryPublishedHighlightsResponse {
  highlights: PublishedHighlightDTO[];
}

interface PublishedHighlightDTO {
  id: string;
  imageId: string;
  links: LinkDTO[];
}

interface LinkDTO {
  type: string;
  id: string;
  name: string;
}

export enum LinkType {
  PATTERN = 'PATTERN',
  FABRIC = 'FABRIC',
}

function toLinkType(value: string): LinkType {
  if (Object.values(LinkType).includes(value as LinkType)) {
    return value as LinkType;
  }
  throw new Error(`Unknown link type: ${value}`);
}

export class Link {
  readonly type: LinkType;
  readonly id: string;
  readonly name: string;

  private constructor(props: { type: LinkType; id: string; name: string }) {
    validateProps(props);

    this.type = props.type;
    this.id = props.id;
    this.name = props.name;
  }

  static of(props: { type: string; id: string; name: string }): Link {
    return new Link({
      type: toLinkType(props.type),
      id: props.id,
      name: props.name,
    });
  }
}

export class PublishedHighlight {
  readonly id: string;
  readonly imageId: string;
  readonly links: Link[];

  private constructor(props: { id: string; imageId: string; links: Link[] }) {
    validateProps(props);

    this.id = props.id;
    this.imageId = props.imageId;
    this.links = props.links;
  }

  static of(props: { id: string; imageId: string; links: Link[] }): PublishedHighlight {
    return new PublishedHighlight({
      id: props.id,
      imageId: props.imageId,
      links: props.links,
    });
  }
}

@Injectable({
  providedIn: 'root',
})
export class HighlightsService {
  constructor(private readonly http: HttpClient) {}

  getPublished(): Observable<PublishedHighlight[]> {
    return this.http
      .get<QueryPublishedHighlightsResponse>(`${environment.apiUrl}/highlights/published`, {
        params: { limit: '3' },
      })
      .pipe(
        map((response) =>
          response.highlights.map((h) =>
            PublishedHighlight.of({
              id: h.id,
              imageId: h.imageId,
              links: h.links.map((l) => Link.of({ type: l.type, id: l.id, name: l.name })),
            })
          )
        )
      );
  }
}
