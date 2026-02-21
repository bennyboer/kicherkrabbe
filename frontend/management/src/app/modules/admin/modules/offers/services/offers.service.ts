import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, Observable} from 'rxjs';
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
  Money,
  Notes,
  NYLON,
  Offer,
  OfferProduct,
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
  PriceHistoryEntry,
  Pricing,
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
import {environment} from '../../../../../../environments';
import {someOrNone} from '@kicherkrabbe/shared';

interface CreateOfferRequest {
  productId: string;
  imageIds: string[];
  notes: NotesDTO;
  price: MoneyDTO;
}

interface CreateOfferResponse {
  id: string;
}

interface UpdateImagesRequest {
  version: number;
  imageIds: string[];
}

interface UpdateImagesResponse {
  version: number;
}

interface UpdateNotesRequest {
  version: number;
  notes: NotesDTO;
}

interface UpdateNotesResponse {
  version: number;
}

interface UpdatePriceRequest {
  version: number;
  price: MoneyDTO;
}

interface UpdatePriceResponse {
  version: number;
}

interface AddDiscountRequest {
  version: number;
  discountedPrice: MoneyDTO;
}

interface AddDiscountResponse {
  version: number;
}

interface RemoveDiscountResponse {
  version: number;
}

interface PublishOfferResponse {
  version: number;
}

interface UnpublishOfferResponse {
  version: number;
}

interface ReserveOfferResponse {
  version: number;
}

interface UnreserveOfferResponse {
  version: number;
}

interface ArchiveOfferResponse {
  version: number;
}

interface QueryOfferResponse {
  offer: OfferDTO;
}

interface QueryOffersRequest {
  searchTerm?: string;
  skip?: number;
  limit?: number;
}

interface QueryOffersResponse {
  total: number;
  offers: OfferDTO[];
}

interface QueryProductsForOfferCreationRequest {
  searchTerm?: string;
  skip?: number;
  limit?: number;
}

interface QueryProductsForOfferCreationResponse {
  total: number;
  products: ProductForOfferCreationDTO[];
}

interface OfferDTO {
  id: string;
  version: number;
  product: ProductDTO;
  imageIds: string[];
  links: LinkDTO[];
  fabricCompositionItems: FabricCompositionItemDTO[];
  pricing: PricingDTO;
  notes: NotesDTO;
  published: boolean;
  reserved: boolean;
  createdAt: string;
  archivedAt?: string;
}

interface ProductDTO {
  id: string;
  number: string;
}

interface PricingDTO {
  price: MoneyDTO;
  discountedPrice?: MoneyDTO;
  priceHistory: PriceHistoryEntryDTO[];
}

interface MoneyDTO {
  amount: number;
  currency: string;
}

interface PriceHistoryEntryDTO {
  price: MoneyDTO;
  timestamp: string;
}

interface NotesDTO {
  description: string;
  contains?: string | null | undefined;
  care?: string | null | undefined;
  safety?: string | null | undefined;
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

export interface ProductForOfferCreation {
  id: string;
  number: string;
  imageIds: string[];
  links: Link[];
  fabricComposition: FabricComposition;
}

interface ProductForOfferCreationDTO {
  id: string;
  number: string;
  imageIds: string[];
  links: LinkDTO[];
  fabricCompositionItems: FabricCompositionItemDTO[];
}

@Injectable()
export class OffersService {
  constructor(private readonly http: HttpClient) {}

  getOffer(offerId: string): Observable<Offer> {
    return this.http
      .get<QueryOfferResponse>(`${environment.apiUrl}/offers/${offerId}`)
      .pipe(map((response) => this.toInternalOffer(response.offer)));
  }

  getOffers(props: {
    searchTerm: string;
    skip?: number;
    limit?: number;
  }): Observable<{ total: number; offers: Offer[] }> {
    const request: QueryOffersRequest = {};

    if (props.searchTerm.trim().length > 0) {
      request.searchTerm = props.searchTerm;
    }
    if (props.skip) {
      request.skip = props.skip;
    }
    if (props.limit) {
      request.limit = props.limit;
    }

    return this.http.post<QueryOffersResponse>(`${environment.apiUrl}/offers`, request).pipe(
      map((response) => ({
        total: response.total,
        offers: response.offers.map((offer) => this.toInternalOffer(offer)),
      })),
    );
  }

  createOffer(props: {
    productId: string;
    imageIds: string[];
    notes: Notes;
    price: Money;
  }): Observable<string> {
    const request: CreateOfferRequest = {
      productId: props.productId,
      imageIds: props.imageIds,
      notes: this.toApiNotes(props.notes),
      price: this.toApiMoney(props.price),
    };

    return this.http
      .post<CreateOfferResponse>(`${environment.apiUrl}/offers/create`, request)
      .pipe(map((response) => response.id));
  }

  deleteOffer(id: string, version: number): Observable<void> {
    return this.http
      .delete<void>(`${environment.apiUrl}/offers/${id}`, {
        params: { version: version.toString() },
      });
  }

  publishOffer(id: string, version: number): Observable<number> {
    return this.http
      .post<PublishOfferResponse>(`${environment.apiUrl}/offers/${id}/publish`, null, {
        params: { version: version.toString() },
      })
      .pipe(map((response) => response.version));
  }

  unpublishOffer(id: string, version: number): Observable<number> {
    return this.http
      .post<UnpublishOfferResponse>(`${environment.apiUrl}/offers/${id}/unpublish`, null, {
        params: { version: version.toString() },
      })
      .pipe(map((response) => response.version));
  }

  reserveOffer(id: string, version: number): Observable<number> {
    return this.http
      .post<ReserveOfferResponse>(`${environment.apiUrl}/offers/${id}/reserve`, null, {
        params: { version: version.toString() },
      })
      .pipe(map((response) => response.version));
  }

  unreserveOffer(id: string, version: number): Observable<number> {
    return this.http
      .post<UnreserveOfferResponse>(`${environment.apiUrl}/offers/${id}/unreserve`, null, {
        params: { version: version.toString() },
      })
      .pipe(map((response) => response.version));
  }

  archiveOffer(id: string, version: number): Observable<number> {
    return this.http
      .post<ArchiveOfferResponse>(`${environment.apiUrl}/offers/${id}/archive`, null, {
        params: { version: version.toString() },
      })
      .pipe(map((response) => response.version));
  }

  updateImages(props: { id: string; version: number; imageIds: string[] }): Observable<number> {
    const request: UpdateImagesRequest = {
      version: props.version,
      imageIds: props.imageIds,
    };

    return this.http
      .post<UpdateImagesResponse>(`${environment.apiUrl}/offers/${props.id}/images/update`, request)
      .pipe(map((response) => response.version));
  }

  updateNotes(props: { id: string; version: number; notes: Notes }): Observable<number> {
    const request: UpdateNotesRequest = {
      version: props.version,
      notes: this.toApiNotes(props.notes),
    };

    return this.http
      .post<UpdateNotesResponse>(`${environment.apiUrl}/offers/${props.id}/notes/update`, request)
      .pipe(map((response) => response.version));
  }

  updatePrice(props: { id: string; version: number; price: Money }): Observable<number> {
    const request: UpdatePriceRequest = {
      version: props.version,
      price: this.toApiMoney(props.price),
    };

    return this.http
      .post<UpdatePriceResponse>(`${environment.apiUrl}/offers/${props.id}/price/update`, request)
      .pipe(map((response) => response.version));
  }

  addDiscount(props: { id: string; version: number; discountedPrice: Money }): Observable<number> {
    const request: AddDiscountRequest = {
      version: props.version,
      discountedPrice: this.toApiMoney(props.discountedPrice),
    };

    return this.http
      .post<AddDiscountResponse>(`${environment.apiUrl}/offers/${props.id}/discount/add`, request)
      .pipe(map((response) => response.version));
  }

  removeDiscount(id: string, version: number): Observable<number> {
    return this.http
      .post<RemoveDiscountResponse>(`${environment.apiUrl}/offers/${id}/discount/remove`, null, {
        params: { version: version.toString() },
      })
      .pipe(map((response) => response.version));
  }

  getProductsForOfferCreation(props: {
    searchTerm: string;
    skip?: number;
    limit?: number;
  }): Observable<{ total: number; products: ProductForOfferCreation[] }> {
    const request: QueryProductsForOfferCreationRequest = {};

    if (props.searchTerm.trim().length > 0) {
      request.searchTerm = props.searchTerm;
    }
    if (props.skip) {
      request.skip = props.skip;
    }
    if (props.limit) {
      request.limit = props.limit;
    }

    return this.http
      .post<QueryProductsForOfferCreationResponse>(`${environment.apiUrl}/offers/products`, request)
      .pipe(
        map((response) => ({
          total: response.total,
          products: response.products.map((p) => this.toInternalProductForCreation(p)),
        })),
      );
  }

  private toInternalOffer(offer: OfferDTO): Offer {
    return Offer.of({
      id: offer.id,
      version: offer.version,
      product: OfferProduct.of({
        id: offer.product.id,
        number: offer.product.number,
      }),
      images: offer.imageIds,
      links: this.toInternalLinks(offer.links),
      fabricComposition: this.toInternalFabricComposition(offer.fabricCompositionItems),
      pricing: this.toInternalPricing(offer.pricing),
      notes: this.toInternalNotes(offer.notes),
      published: offer.published,
      reserved: offer.reserved,
      createdAt: new Date(offer.createdAt),
      archivedAt: someOrNone(offer.archivedAt).map((d) => new Date(d)).orElseNull(),
    });
  }

  private toInternalPricing(pricing: PricingDTO): Pricing {
    return Pricing.of({
      price: this.toInternalMoney(pricing.price),
      discountedPrice: someOrNone(pricing.discountedPrice).map((m) => this.toInternalMoney(m)).orElseNull(),
      priceHistory: pricing.priceHistory.map((entry) => this.toInternalPriceHistoryEntry(entry)),
    });
  }

  private toInternalPriceHistoryEntry(entry: PriceHistoryEntryDTO): PriceHistoryEntry {
    return PriceHistoryEntry.of({
      price: this.toInternalMoney(entry.price),
      timestamp: new Date(entry.timestamp),
    });
  }

  private toInternalMoney(money: MoneyDTO): Money {
    return Money.of({
      amount: money.amount,
      currency: money.currency,
    });
  }

  private toApiMoney(money: Money): MoneyDTO {
    return {
      amount: money.amount,
      currency: money.currency,
    };
  }

  private toInternalNotes(notes: NotesDTO): Notes {
    return Notes.of({
      description: notes.description,
      contains: notes.contains,
      care: notes.care,
      safety: notes.safety,
    });
  }

  private toApiNotes(notes: Notes): NotesDTO {
    return {
      description: notes.description,
      contains: notes.contains.orElseNull(),
      care: notes.care.orElseNull(),
      safety: notes.safety.orElseNull(),
    };
  }

  private toInternalLinks(links: LinkDTO[]): Link[] {
    return links.map((link) => this.toInternalLink(link));
  }

  private toInternalLink(link: LinkDTO): Link {
    return Link.of({
      type: this.toInternalLinkType(link.type),
      name: link.name,
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

  private toInternalFabricComposition(items: FabricCompositionItemDTO[]): FabricComposition {
    return FabricComposition.of({
      items: items.map((item) => this.toInternalFabricCompositionItem(item)),
    });
  }

  private toInternalFabricCompositionItem(item: FabricCompositionItemDTO): FabricCompositionItem {
    return FabricCompositionItem.of({
      fabricType: this.toInternalFabricType(item.fabricType),
      percentage: item.percentage,
    });
  }

  private toInternalProductForCreation(product: ProductForOfferCreationDTO): ProductForOfferCreation {
    return {
      id: product.id,
      number: product.number,
      imageIds: product.imageIds ?? [],
      links: this.toInternalLinks(product.links),
      fabricComposition: this.toInternalFabricComposition(product.fabricCompositionItems),
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
}
