import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError, map, Observable, of } from 'rxjs';
import {
  ABACA,
  ACRYLIC,
  ALFA,
  ALPAKA,
  ANGORA_GOAT,
  ANGORA_WOOL,
  ARAMID,
  BAMBOO,
  BEAVER,
  CAMEL,
  CARBON_FIBER,
  CASHGORA_GOAT,
  CASHMERE,
  CELLULOSE_ACETATE,
  CHLORO_FIBER,
  COCONUT,
  COTTON,
  COW_HAIR,
  CUPRO,
  ELASTANE,
  FABRIC,
  FabricComposition,
  FabricCompositionItem,
  FabricType,
  FLUOR_FIBER,
  GOAT_HAIR,
  HAIR,
  HALF_LINEN,
  HEMP,
  HENEQUEN,
  HORSE_HAIR,
  InternalFabricType,
  JUTE,
  KAPOK,
  KENAF,
  LAMA,
  LINEN,
  Link,
  LinkType,
  LUREX,
  LYOCELL,
  MAGUEY,
  MODACRYLIC,
  MODAL,
  Notes,
  NYLON,
  OTTER,
  PAPER,
  PATTERN,
  POLYAMIDE,
  POLYCARBAMIDE,
  POLYESTER,
  POLYETHYLENE,
  POLYPROPYLENE,
  POLYURETHANE,
  POLYVINYL_CHLORIDE,
  Product,
  RAMIE,
  SILK,
  SISAL,
  SUNN,
  TETORON_COTTON,
  TRIACETATE,
  TRIVINYL,
  UNKNOWN,
  VINYL,
  VIRGIN_WOOL,
  VISCOSE,
  WOOL,
  YAK,
} from '../model';
import { environment } from '../../../../../../environments';
import { someOrNone } from '../../../../shared/modules/option';

interface CreateProductRequest {
  images: string[];
  links: LinkDTO[];
  fabricComposition: FabricCompositionDTO;
  notes: NotesDTO;
  producedAt: string;
}

interface CreateProductResponse {
  id: string;
  version: number;
}

interface AddLinkRequest {
  version: number;
  linkType: LinkTypeDTO;
  linkId: string;
}

interface AddLinkResponse {
  version: number;
}

interface RemoveLinkResponse {
  version: number;
}

interface UpdateFabricCompositionRequest {
  version: number;
  fabricComposition: FabricCompositionDTO;
}

interface UpdateFabricCompositionResponse {
  version: number;
}

interface UpdateNotesRequest {
  version: number;
  notes: NotesDTO;
}

interface UpdateNotesResponse {
  version: number;
}

interface UpdateImagesRequest {
  version: number;
  images: string[];
}

interface UpdateImagesResponse {
  version: number;
}

interface UpdateProducedAtDateRequest {
  version: number;
  producedAt: string;
}

interface UpdateProducedAtDateResponse {
  version: number;
}

interface QueryLinksResponse {
  total: number;
  links: LinkDTO[];
}

interface QueryProductResponse {
  product: ProductDTO;
}

interface QueryProductsResponse {
  total: number;
  products: ProductDTO[];
}

interface DeleteProductResponse {
  version: number;
}

interface ProductDTO {
  id: string;
  version: number;
  number: string;
  images: string[];
  links: LinkDTO[];
  fabricComposition: FabricCompositionDTO;
  notes: NotesDTO;
  producedAt: string;
  createdAt: string;
}

interface LinkDTO {
  type: LinkTypeDTO;
  name: string;
  id: string;
}

enum LinkTypeDTO {
  PATTERN = 'PATTERN',
  FABRIC = 'FABRIC',
}

interface FabricCompositionDTO {
  items: FabricCompositionItemDTO[];
}

interface FabricCompositionItemDTO {
  fabricType: FabricTypeDTO;
  percentage: number;
}

enum FabricTypeDTO {
  ABACA = 'ABACA',
  ALFA = 'ALFA',
  BAMBOO = 'BAMBOO',
  HEMP = 'HEMP',
  COTTON = 'COTTON',
  COCONUT = 'COCONUT',
  CASHMERE = 'CASHMERE',
  HENEQUEN = 'HENEQUEN',
  HALF_LINEN = 'HALF_LINEN',
  JUTE = 'JUTE',
  KENAF = 'KENAF',
  KAPOK = 'KAPOK',
  LINEN = 'LINEN',
  MAGUEY = 'MAGUEY',
  RAMIE = 'RAMIE',
  SISAL = 'SISAL',
  SUNN = 'SUNN',
  CELLULOSE_ACETATE = 'CELLULOSE_ACETATE',
  CUPRO = 'CUPRO',
  LYOCELL = 'LYOCELL',
  MODAL = 'MODAL',
  PAPER = 'PAPER',
  TRIACETATE = 'TRIACETATE',
  VISCOSE = 'VISCOSE',
  ARAMID = 'ARAMID',
  CARBON_FIBER = 'CARBON_FIBER',
  CHLORO_FIBER = 'CHLORO_FIBER',
  ELASTANE = 'ELASTANE',
  FLUOR_FIBER = 'FLUOR_FIBER',
  LUREX = 'LUREX',
  MODACRYLIC = 'MODACRYLIC',
  NYLON = 'NYLON',
  POLYAMIDE = 'POLYAMIDE',
  POLYCARBAMIDE = 'POLYCARBAMIDE',
  ACRYLIC = 'ACRYLIC',
  POLYETHYLENE = 'POLYETHYLENE',
  POLYESTER = 'POLYESTER',
  POLYPROPYLENE = 'POLYPROPYLENE',
  POLYURETHANE = 'POLYURETHANE',
  POLYVINYL_CHLORIDE = 'POLYVINYL_CHLORIDE',
  TETORON_COTTON = 'TETORON_COTTON',
  TRIVINYL = 'TRIVINYL',
  VINYL = 'VINYL',
  HAIR = 'HAIR',
  COW_HAIR = 'COW_HAIR',
  HORSE_HAIR = 'HORSE_HAIR',
  GOAT_HAIR = 'GOAT_HAIR',
  SILK = 'SILK',
  ANGORA_WOOL = 'ANGORA_WOOL',
  BEAVER = 'BEAVER',
  CASHGORA_GOAT = 'CASHGORA_GOAT',
  CAMEL = 'CAMEL',
  LAMA = 'LAMA',
  ANGORA_GOAT = 'ANGORA_GOAT',
  WOOL = 'WOOL',
  ALPAKA = 'ALPAKA',
  OTTER = 'OTTER',
  VIRGIN_WOOL = 'VIRGIN_WOOL',
  YAK = 'YAK',
  UNKNOWN = 'UNKNOWN',
}

interface NotesDTO {
  contains: string;
  care: string;
  safety: string;
}

@Injectable()
export class ProductsService {
  constructor(private readonly http: HttpClient) {}

  getProduct(productId: string): Observable<Product> {
    return this.http.get<QueryProductResponse>(`${environment.apiUrl}/products/${productId}`).pipe(
      map((response) => this.toInternalProduct(response.product)),
      // TODO Remove error handler once backend is implemented
      catchError((_) => {
        return of(
          this.toInternalProduct({
            id: 'PRODUCT_ID_1',
            version: 1,
            number: '0000000001',
            images: [
              '14dc190d-5be8-4285-883e-408741b28723',
              '7eff4b41-e828-47e8-8a8e-7e11e68e56db',
              '01efafd3-41d6-4a85-a4f3-8777405ac37b',
            ],
            links: [
              {
                type: LinkTypeDTO.PATTERN,
                name: 'Pattern 1',
                id: 'PATTERN_ID_1',
              },
              {
                type: LinkTypeDTO.FABRIC,
                name: 'Fabric 1',
                id: 'FABRIC_ID_1',
              },
            ],
            fabricComposition: {
              items: [
                {
                  fabricType: FabricTypeDTO.COTTON,
                  percentage: 80,
                },
                {
                  fabricType: FabricTypeDTO.POLYESTER,
                  percentage: 20,
                },
              ],
            },
            notes: {
              contains: '{"ops":[{"insert":"Stoff kommt von Hersteller soundso, etc. etc."}]}',
              care: '{"ops":[{"insert":"Nicht über 30 Grad waschen, nicht in den Trockner, blabla"}]}',
              safety:
                '{"ops":[{"attributes":{"bold":true},"insert":"Achtung"},{"insert":"! Verschluckungsgefahr (Knöpfe)! Nur unter Aufsicht von Erwachsenen benutzen."}]}',
            },
            producedAt: '2024-12-28T12:30:00Z',
            createdAt: '2024-12-28T13:00:00Z',
          }),
        );
      }),
    );
  }

  getProducts(props: {
    searchValue: string;
    from?: Date | null;
    to?: Date | null;
    skip?: number;
    limit?: number;
  }): Observable<{ total: number; products: Product[] }> {
    let params = new HttpParams();

    if (props.searchValue.trim().length > 0) {
      params = params.set('searchValue', props.searchValue);
    }
    if (props.from) {
      params = params.set('from', props.from.toISOString());
    }
    if (props.to) {
      params = params.set('to', props.to.toISOString());
    }
    if (props.skip) {
      params = params.set('skip', props.skip.toString());
    }
    if (props.limit) {
      params = params.set('limit', props.limit.toString());
    }

    return this.http.get<QueryProductsResponse>(`${environment.apiUrl}/products`, { params }).pipe(
      map((response) => {
        return {
          total: response.total,
          products: this.toInternalProducts(response.products),
        };
      }),
      // TODO Remove error handler once backend is implemented
      catchError((_) => {
        return of({
          total: 2,
          products: this.toInternalProducts([
            {
              id: 'PRODUCT_ID_1',
              version: 1,
              number: '0000000001',
              images: [
                '14dc190d-5be8-4285-883e-408741b28723',
                '7eff4b41-e828-47e8-8a8e-7e11e68e56db',
                '01efafd3-41d6-4a85-a4f3-8777405ac37b',
              ],
              links: [
                {
                  type: LinkTypeDTO.PATTERN,
                  name: 'Pattern 1',
                  id: 'PATTERN_ID_1',
                },
                {
                  type: LinkTypeDTO.FABRIC,
                  name: 'Fabric 1',
                  id: 'FABRIC_ID_1',
                },
              ],
              fabricComposition: {
                items: [
                  {
                    fabricType: FabricTypeDTO.COTTON,
                    percentage: 80,
                  },
                  {
                    fabricType: FabricTypeDTO.POLYESTER,
                    percentage: 20,
                  },
                ],
              },
              notes: {
                contains: '{"ops":[{"insert":"Stoff kommt von Hersteller soundso, etc. etc."}]}',
                care: '{"ops":[{"insert":"Nicht über 30 Grad waschen, nicht in den Trockner, blabla"}]}',
                safety:
                  '{"ops":[{"attributes":{"bold":true},"insert":"Achtung"},{"insert":"! Verschluckungsgefahr (Knöpfe)! Nur unter Aufsicht von Erwachsenen benutzen."}]}',
              },
              producedAt: '2024-12-28T12:30:00Z',
              createdAt: '2024-12-28T13:00:00Z',
            },
            {
              id: 'PRODUCT_ID_2',
              version: 0,
              number: '0000000002',
              images: [],
              links: [
                {
                  type: LinkTypeDTO.PATTERN,
                  name: 'Pattern 2',
                  id: 'PATTERN_ID_2',
                },
                {
                  type: LinkTypeDTO.FABRIC,
                  name: 'Fabric 2',
                  id: 'FABRIC_ID_2',
                },
              ],
              fabricComposition: {
                items: [
                  {
                    fabricType: FabricTypeDTO.ELASTANE,
                    percentage: 60,
                  },
                  {
                    fabricType: FabricTypeDTO.POLYESTER,
                    percentage: 40,
                  },
                ],
              },
              notes: {
                contains: 'Contains',
                care: 'Care',
                safety: 'Safety',
              },
              producedAt: '2024-12-29T12:30:00Z',
              createdAt: '2024-12-29T13:00:00Z',
            },
          ]),
        });
      }),
    );
  }

  deleteProduct(id: string, version: number): Observable<number> {
    return this.http
      .delete<DeleteProductResponse>(`${environment.apiUrl}/products/${id}`, {
        params: { version: version.toString() },
      })
      .pipe(map((response) => response.version));
  }

  getLinks(props: {
    searchTerm?: string;
    skip?: number;
    limit?: number;
  }): Observable<{ total: number; links: Link[] }> {
    const searchTerm = someOrNone(props.searchTerm).orElse('');
    const skip = someOrNone(props.skip).orElse(0);
    const limit = someOrNone(props.limit).orElse(10);

    let params = new HttpParams();
    if (searchTerm.length > 0) {
      params = params.set('searchTerm', searchTerm);
    }
    if (skip > 0) {
      params = params.set('skip', skip.toString());
    }
    params = params.set('limit', limit.toString());

    return this.http.get<QueryLinksResponse>(`${environment.apiUrl}/products/links`, { params }).pipe(
      map((response) => ({
        total: response.total,
        links: response.links.map((link) => this.toInternalLink(link)),
      })),
      // TODO Remove error handler once backend is implemented
      catchError((_) => {
        const MOCK_FILTERED_LINKS = [
          Link.of({
            type: PATTERN,
            name: 'Pattern 1',
            id: 'PATTERN_ID_1',
          }),
          Link.of({
            type: FABRIC,
            name: 'Fabric 1',
            id: 'FABRIC_ID_1',
          }),
          Link.of({
            type: PATTERN,
            name: 'Pattern 2',
            id: 'PATTERN_ID_2',
          }),
          Link.of({
            type: FABRIC,
            name: 'Ein Stoff mit einem sehr langen Namen, der über mehrere Zeilen geht',
            id: 'FABRIC_ID_2',
          }),
        ].filter((link) => link.name.includes(searchTerm));

        return of({
          total: MOCK_FILTERED_LINKS.length,
          links: MOCK_FILTERED_LINKS.filter((_, index) => index >= skip && index < skip + limit),
        });
      }),
    );
  }

  addLink(props: { id: string; version: number; linkType: LinkType; linkId: string }): Observable<number> {
    const productId = props.id;
    const request: AddLinkRequest = {
      version: props.version,
      linkType: this.toApiLinkType(props.linkType),
      linkId: props.linkId,
    };

    return this.http.post<AddLinkResponse>(`${environment.apiUrl}/products/${productId}/links/add`, request).pipe(
      map((response) => response.version),
      // TODO Remove error handler once backend is implemented
      catchError((_) => of(props.version + 1)),
    );
  }

  removeLink(props: { id: string; version: number; linkType: LinkType; linkId: string }): Observable<number> {
    const productId = props.id;
    const version = props.version;
    const linkId = props.linkId;
    const linkType = this.toApiLinkType(props.linkType);

    return this.http
      .delete<RemoveLinkResponse>(`${environment.apiUrl}/products/${productId}/links/${linkType}/${linkId}`, {
        params: { version: version.toString() },
      })
      .pipe(
        map((response) => response.version),
        // TODO Remove error handler once backend is implemented
        catchError((_) => of(version + 1)),
      );
  }

  updateFabricComposition(props: {
    id: string;
    version: number;
    fabricComposition: FabricComposition;
  }): Observable<number> {
    const productId = props.id;
    const request: UpdateFabricCompositionRequest = {
      version: props.version,
      fabricComposition: this.toApiFabricComposition(props.fabricComposition),
    };

    return this.http
      .post<UpdateFabricCompositionResponse>(
        `${environment.apiUrl}/products/${productId}/fabric-composition/update`,
        request,
      )
      .pipe(
        map((response) => response.version),
        // TODO Remove error handler once backend is implemented
        catchError((_) => of(props.version + 1)),
      );
  }

  updateNotes(props: { id: string; version: number; notes: Notes }): Observable<number> {
    const productId = props.id;
    const request: UpdateNotesRequest = {
      version: props.version,
      notes: this.toApiNotes(props.notes),
    };

    return this.http
      .post<UpdateNotesResponse>(`${environment.apiUrl}/products/${productId}/notes/update`, request)
      .pipe(
        map((response) => response.version),
        // TODO Remove error handler once backend is implemented
        catchError((_) => of(props.version + 1)),
      );
  }

  updateProducedAtDate(props: { id: string; version: number; date: Date }): Observable<number> {
    const productId = props.id;
    const request: UpdateProducedAtDateRequest = {
      version: props.version,
      producedAt: props.date.toISOString(),
    };

    return this.http
      .post<UpdateProducedAtDateResponse>(`${environment.apiUrl}/products/${productId}/produced-at/update`, request)
      .pipe(
        map((response) => response.version),
        // TODO Remove error handler once backend is implemented
        catchError((_) => of(props.version + 1)),
      );
  }

  updateImages(props: { id: string; version: number; imageIds: string[] }): Observable<number> {
    const productId = props.id;
    const request: UpdateImagesRequest = {
      version: props.version,
      images: props.imageIds,
    };

    return this.http
      .post<UpdateImagesResponse>(`${environment.apiUrl}/products/${productId}/images/update`, request)
      .pipe(
        map((response) => response.version),
        // TODO Remove error handler once backend is implemented
        catchError((_) => of(props.version + 1)),
      );
  }

  createProduct(props: {
    images: string[];
    links: Link[];
    fabricComposition: FabricComposition;
    notes: Notes;
    producedAt: Date;
  }): Observable<string> {
    const request: CreateProductRequest = {
      images: props.images,
      links: this.toApiLinks(props.links),
      fabricComposition: this.toApiFabricComposition(props.fabricComposition),
      notes: this.toApiNotes(props.notes),
      producedAt: props.producedAt.toISOString(),
    };

    return this.http.post<CreateProductResponse>(`${environment.apiUrl}/products/create`, request).pipe(
      map((response) => response.id),
      // TODO Remove error handler once backend is implemented
      catchError((_) => of('PRODUCT_ID_1')),
    );
  }

  private toInternalProducts(products: ProductDTO[]): Product[] {
    return products.map((product) => this.toInternalProduct(product));
  }

  private toInternalProduct(product: ProductDTO): Product {
    return Product.of({
      id: product.id,
      version: product.version,
      number: product.number,
      images: product.images,
      links: this.toInternalLinks(product.links),
      fabricComposition: this.toInternalFabricComposition(product.fabricComposition),
      notes: this.toInternalNotes(product.notes),
      producedAt: new Date(product.producedAt),
      createdAt: new Date(product.createdAt),
    });
  }

  private toInternalNotes(notes: NotesDTO): Notes {
    return Notes.of({
      contains: notes.contains,
      care: notes.care,
      safety: notes.safety,
    });
  }

  private toApiNotes(notes: Notes): NotesDTO {
    return {
      contains: notes.contains,
      care: notes.care,
      safety: notes.safety,
    };
  }

  private toInternalLinks(links: LinkDTO[]): Link[] {
    return links.map((link) => this.toInternalLink(link));
  }

  private toApiLinks(links: Link[]): LinkDTO[] {
    return links.map((link) => this.toApiLink(link));
  }

  private toInternalLink(link: LinkDTO): Link {
    return Link.of({
      type: this.toInternalLinkType(link.type),
      name: link.name,
      id: link.id,
    });
  }

  private toApiLink(link: Link): LinkDTO {
    return {
      type: this.toApiLinkType(link.type),
      name: link.name,
      id: link.id,
    };
  }

  private toInternalLinkType(type: LinkTypeDTO): LinkType {
    switch (type) {
      case LinkTypeDTO.PATTERN:
        return PATTERN;
      case LinkTypeDTO.FABRIC:
        return FABRIC;
      default:
        throw new Error(`Unknown link type: ${type}`);
    }
  }

  private toApiLinkType(type: LinkType): LinkTypeDTO {
    switch (type) {
      case PATTERN:
        return LinkTypeDTO.PATTERN;
      case FABRIC:
        return LinkTypeDTO.FABRIC;
      default:
        throw new Error(`Unknown link type: ${type}`);
    }
  }

  private toInternalFabricComposition(composition: FabricCompositionDTO): FabricComposition {
    return FabricComposition.of({
      items: this.toInternalFabricCompositionItems(composition.items),
    });
  }

  private toApiFabricComposition(composition: FabricComposition): FabricCompositionDTO {
    return {
      items: this.toApiFabricCompositionItems(composition.items),
    };
  }

  private toInternalFabricCompositionItems(items: FabricCompositionItemDTO[]): FabricCompositionItem[] {
    return items.map((item) => this.toInternalFabricCompositionItem(item));
  }

  private toApiFabricCompositionItems(items: FabricCompositionItem[]): FabricCompositionItemDTO[] {
    return items.map((item) => this.toApiFabricCompositionItem(item));
  }

  private toInternalFabricCompositionItem(item: FabricCompositionItemDTO): FabricCompositionItem {
    return FabricCompositionItem.of({
      fabricType: this.toInternalFabricType(item.fabricType),
      percentage: item.percentage,
    });
  }

  private toApiFabricCompositionItem(item: FabricCompositionItem): FabricCompositionItemDTO {
    return {
      fabricType: this.toApiFabricType(item.fabricType),
      percentage: item.percentage,
    };
  }

  private toInternalFabricType(type: FabricTypeDTO): FabricType {
    switch (type) {
      case FabricTypeDTO.ABACA:
        return ABACA;
      case FabricTypeDTO.ALFA:
        return ALFA;
      case FabricTypeDTO.BAMBOO:
        return BAMBOO;
      case FabricTypeDTO.HEMP:
        return HEMP;
      case FabricTypeDTO.COTTON:
        return COTTON;
      case FabricTypeDTO.COCONUT:
        return COCONUT;
      case FabricTypeDTO.CASHMERE:
        return CASHMERE;
      case FabricTypeDTO.HENEQUEN:
        return HENEQUEN;
      case FabricTypeDTO.HALF_LINEN:
        return HALF_LINEN;
      case FabricTypeDTO.JUTE:
        return JUTE;
      case FabricTypeDTO.KENAF:
        return KENAF;
      case FabricTypeDTO.KAPOK:
        return KAPOK;
      case FabricTypeDTO.LINEN:
        return LINEN;
      case FabricTypeDTO.MAGUEY:
        return MAGUEY;
      case FabricTypeDTO.RAMIE:
        return RAMIE;
      case FabricTypeDTO.SISAL:
        return SISAL;
      case FabricTypeDTO.SUNN:
        return SUNN;
      case FabricTypeDTO.CELLULOSE_ACETATE:
        return CELLULOSE_ACETATE;
      case FabricTypeDTO.CUPRO:
        return CUPRO;
      case FabricTypeDTO.LYOCELL:
        return LYOCELL;
      case FabricTypeDTO.MODAL:
        return MODAL;
      case FabricTypeDTO.PAPER:
        return PAPER;
      case FabricTypeDTO.TRIACETATE:
        return TRIACETATE;
      case FabricTypeDTO.VISCOSE:
        return VISCOSE;
      case FabricTypeDTO.ARAMID:
        return ARAMID;
      case FabricTypeDTO.CARBON_FIBER:
        return CARBON_FIBER;
      case FabricTypeDTO.CHLORO_FIBER:
        return CHLORO_FIBER;
      case FabricTypeDTO.ELASTANE:
        return ELASTANE;
      case FabricTypeDTO.FLUOR_FIBER:
        return FLUOR_FIBER;
      case FabricTypeDTO.LUREX:
        return LUREX;
      case FabricTypeDTO.MODACRYLIC:
        return MODACRYLIC;
      case FabricTypeDTO.NYLON:
        return NYLON;
      case FabricTypeDTO.POLYAMIDE:
        return POLYAMIDE;
      case FabricTypeDTO.POLYCARBAMIDE:
        return POLYCARBAMIDE;
      case FabricTypeDTO.ACRYLIC:
        return ACRYLIC;
      case FabricTypeDTO.POLYETHYLENE:
        return POLYETHYLENE;
      case FabricTypeDTO.POLYESTER:
        return POLYESTER;
      case FabricTypeDTO.POLYPROPYLENE:
        return POLYPROPYLENE;
      case FabricTypeDTO.POLYURETHANE:
        return POLYURETHANE;
      case FabricTypeDTO.POLYVINYL_CHLORIDE:
        return POLYVINYL_CHLORIDE;
      case FabricTypeDTO.TETORON_COTTON:
        return TETORON_COTTON;
      case FabricTypeDTO.TRIVINYL:
        return TRIVINYL;
      case FabricTypeDTO.VINYL:
        return VINYL;
      case FabricTypeDTO.HAIR:
        return HAIR;
      case FabricTypeDTO.COW_HAIR:
        return COW_HAIR;
      case FabricTypeDTO.HORSE_HAIR:
        return HORSE_HAIR;
      case FabricTypeDTO.GOAT_HAIR:
        return GOAT_HAIR;
      case FabricTypeDTO.SILK:
        return SILK;
      case FabricTypeDTO.ANGORA_WOOL:
        return ANGORA_WOOL;
      case FabricTypeDTO.BEAVER:
        return BEAVER;
      case FabricTypeDTO.CASHGORA_GOAT:
        return CASHGORA_GOAT;
      case FabricTypeDTO.CAMEL:
        return CAMEL;
      case FabricTypeDTO.LAMA:
        return LAMA;
      case FabricTypeDTO.ANGORA_GOAT:
        return ANGORA_GOAT;
      case FabricTypeDTO.WOOL:
        return WOOL;
      case FabricTypeDTO.ALPAKA:
        return ALPAKA;
      case FabricTypeDTO.OTTER:
        return OTTER;
      case FabricTypeDTO.VIRGIN_WOOL:
        return VIRGIN_WOOL;
      case FabricTypeDTO.YAK:
        return YAK;
      case FabricTypeDTO.UNKNOWN:
      default:
        return UNKNOWN;
    }
  }

  private toApiFabricType(type: FabricType): FabricTypeDTO {
    switch (type.internal) {
      case InternalFabricType.ABACA:
        return FabricTypeDTO.ABACA;
      case InternalFabricType.ALFA:
        return FabricTypeDTO.ALFA;
      case InternalFabricType.BAMBOO:
        return FabricTypeDTO.BAMBOO;
      case InternalFabricType.HEMP:
        return FabricTypeDTO.HEMP;
      case InternalFabricType.COTTON:
        return FabricTypeDTO.COTTON;
      case InternalFabricType.COCONUT:
        return FabricTypeDTO.COCONUT;
      case InternalFabricType.CASHMERE:
        return FabricTypeDTO.CASHMERE;
      case InternalFabricType.HENEQUEN:
        return FabricTypeDTO.HENEQUEN;
      case InternalFabricType.HALF_LINEN:
        return FabricTypeDTO.HALF_LINEN;
      case InternalFabricType.JUTE:
        return FabricTypeDTO.JUTE;
      case InternalFabricType.KENAF:
        return FabricTypeDTO.KENAF;
      case InternalFabricType.KAPOK:
        return FabricTypeDTO.KAPOK;
      case InternalFabricType.LINEN:
        return FabricTypeDTO.LINEN;
      case InternalFabricType.MAGUEY:
        return FabricTypeDTO.MAGUEY;
      case InternalFabricType.RAMIE:
        return FabricTypeDTO.RAMIE;
      case InternalFabricType.SISAL:
        return FabricTypeDTO.SISAL;
      case InternalFabricType.SUNN:
        return FabricTypeDTO.SUNN;
      case InternalFabricType.CELLULOSE_ACETATE:
        return FabricTypeDTO.CELLULOSE_ACETATE;
      case InternalFabricType.CUPRO:
        return FabricTypeDTO.CUPRO;
      case InternalFabricType.LYOCELL:
        return FabricTypeDTO.LYOCELL;
      case InternalFabricType.MODAL:
        return FabricTypeDTO.MODAL;
      case InternalFabricType.PAPER:
        return FabricTypeDTO.PAPER;
      case InternalFabricType.TRIACETATE:
        return FabricTypeDTO.TRIACETATE;
      case InternalFabricType.VISCOSE:
        return FabricTypeDTO.VISCOSE;
      case InternalFabricType.ARAMID:
        return FabricTypeDTO.ARAMID;
      case InternalFabricType.CARBON_FIBER:
        return FabricTypeDTO.CARBON_FIBER;
      case InternalFabricType.CHLORO_FIBER:
        return FabricTypeDTO.CHLORO_FIBER;
      case InternalFabricType.ELASTANE:
        return FabricTypeDTO.ELASTANE;
      case InternalFabricType.FLUOR_FIBER:
        return FabricTypeDTO.FLUOR_FIBER;
      case InternalFabricType.LUREX:
        return FabricTypeDTO.LUREX;
      case InternalFabricType.MODACRYLIC:
        return FabricTypeDTO.MODACRYLIC;
      case InternalFabricType.NYLON:
        return FabricTypeDTO.NYLON;
      case InternalFabricType.POLYAMIDE:
        return FabricTypeDTO.POLYAMIDE;
      case InternalFabricType.POLYCARBAMIDE:
        return FabricTypeDTO.POLYCARBAMIDE;
      case InternalFabricType.ACRYLIC:
        return FabricTypeDTO.ACRYLIC;
      case InternalFabricType.POLYETHYLENE:
        return FabricTypeDTO.POLYETHYLENE;
      case InternalFabricType.POLYESTER:
        return FabricTypeDTO.POLYESTER;
      case InternalFabricType.POLYPROPYLENE:
        return FabricTypeDTO.POLYPROPYLENE;
      case InternalFabricType.POLYURETHANE:
        return FabricTypeDTO.POLYURETHANE;
      case InternalFabricType.POLYVINYL_CHLORIDE:
        return FabricTypeDTO.POLYVINYL_CHLORIDE;
      case InternalFabricType.TETORON_COTTON:
        return FabricTypeDTO.TETORON_COTTON;
      case InternalFabricType.TRIVINYL:
        return FabricTypeDTO.TRIVINYL;
      case InternalFabricType.VINYL:
        return FabricTypeDTO.VINYL;
      case InternalFabricType.HAIR:
        return FabricTypeDTO.HAIR;
      case InternalFabricType.COW_HAIR:
        return FabricTypeDTO.COW_HAIR;
      case InternalFabricType.HORSE_HAIR:
        return FabricTypeDTO.HORSE_HAIR;
      case InternalFabricType.GOAT_HAIR:
        return FabricTypeDTO.GOAT_HAIR;
      case InternalFabricType.SILK:
        return FabricTypeDTO.SILK;
      case InternalFabricType.ANGORA_WOOL:
        return FabricTypeDTO.ANGORA_WOOL;
      case InternalFabricType.BEAVER:
        return FabricTypeDTO.BEAVER;
      case InternalFabricType.CASHGORA_GOAT:
        return FabricTypeDTO.CASHGORA_GOAT;
      case InternalFabricType.CAMEL:
        return FabricTypeDTO.CAMEL;
      case InternalFabricType.LAMA:
        return FabricTypeDTO.LAMA;
      case InternalFabricType.ANGORA_GOAT:
        return FabricTypeDTO.ANGORA_GOAT;
      case InternalFabricType.WOOL:
        return FabricTypeDTO.WOOL;
      case InternalFabricType.ALPAKA:
        return FabricTypeDTO.ALPAKA;
      case InternalFabricType.OTTER:
        return FabricTypeDTO.OTTER;
      case InternalFabricType.VIRGIN_WOOL:
        return FabricTypeDTO.VIRGIN_WOOL;
      case InternalFabricType.YAK:
        return FabricTypeDTO.YAK;
      case InternalFabricType.UNKNOWN:
        throw new Error('Unknown fabric type');
    }
  }
}
