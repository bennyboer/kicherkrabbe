import { OffersService } from './offers.service';
import { OfferCategoriesService } from './offer-categories.service';

export { OffersService };
export { OfferCategoriesService };
export type { ProductForOfferCreation } from './offers.service';

export const SERVICES = [OffersService, OfferCategoriesService];
