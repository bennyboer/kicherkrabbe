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

interface QueryProductsResponse {
  total: number;
  products: ProductDTO[];
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
              number: 'PRODUCT_NUMBER_1',
              images: ['2905e300-f4e1-4fcc-94b0-28a243d3b54f'],
              links: [
                {
                  type: LinkTypeDTO.PATTERN,
                  id: 'PATTERN_ID_1',
                },
                {
                  type: LinkTypeDTO.FABRIC,
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
                contains: 'Contains',
                care: 'Care',
                safety: 'Safety',
              },
              producedAt: '2024-12-28T12:30:00Z',
              createdAt: '2024-12-28T13:00:00Z',
            },
            {
              id: 'PRODUCT_ID_2',
              version: 0,
              number: 'PRODUCT_NUMBER_2',
              images: [],
              links: [
                {
                  type: LinkTypeDTO.PATTERN,
                  id: 'PATTERN_ID_2',
                },
                {
                  type: LinkTypeDTO.FABRIC,
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

  private toInternalLinks(links: LinkDTO[]): Link[] {
    return links.map((link) => this.toInternalLink(link));
  }

  private toInternalLink(link: LinkDTO): Link {
    return Link.of({
      type: this.toInternalLinkType(link.type),
      id: link.id,
    });
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

  private toInternalFabricComposition(composition: FabricCompositionDTO): FabricComposition {
    return FabricComposition.of({
      items: this.toInternalFabricCompositionItems(composition.items),
    });
  }

  private toInternalFabricCompositionItems(items: FabricCompositionItemDTO[]): FabricCompositionItem[] {
    return items.map((item) => this.toInternalFabricCompositionItem(item));
  }

  private toInternalFabricCompositionItem(item: FabricCompositionItemDTO): FabricCompositionItem {
    return FabricCompositionItem.of({
      fabricType: this.toInternalFabricType(item.fabricType),
      percentage: item.percentage,
    });
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
}
