import { none, someOrNone, type Option, validateProps } from "@kicherkrabbe/shared";
import type {
	FabricCompositionItem,
	Money,
	Notes,
	OfferLink,
	Pricing,
} from "./model";

export class Offer {
	readonly id: string;
	readonly alias: string;
	readonly title: string;
	readonly size: string;
	readonly categoryIds: string[];
	readonly images: string[];
	readonly links: OfferLink[];
	readonly fabricCompositionItems: FabricCompositionItem[];
	readonly pricing: Pricing;
	readonly notes: Notes;
	readonly reserved: boolean;

	private constructor(props: {
		id: string;
		alias: string;
		title: string;
		size: string;
		categoryIds: string[];
		images: string[];
		links: OfferLink[];
		fabricCompositionItems: FabricCompositionItem[];
		pricing: Pricing;
		notes: Notes;
		reserved: boolean;
	}) {
		validateProps(props);

		this.id = props.id;
		this.alias = props.alias;
		this.title = props.title;
		this.size = props.size;
		this.categoryIds = props.categoryIds;
		this.images = props.images;
		this.links = props.links;
		this.fabricCompositionItems = props.fabricCompositionItems;
		this.pricing = props.pricing;
		this.notes = props.notes;
		this.reserved = props.reserved;
	}

	static of(props: {
		id: string;
		alias: string;
		title: string;
		size: string;
		categoryIds?: string[];
		images: string[];
		links?: OfferLink[];
		fabricCompositionItems?: FabricCompositionItem[];
		pricing: Pricing;
		notes: Notes;
		reserved?: boolean;
	}): Offer {
		return new Offer({
			id: props.id,
			alias: props.alias,
			title: props.title,
			size: props.size,
			categoryIds: props.categoryIds ?? [],
			images: props.images,
			links: props.links ?? [],
			fabricCompositionItems: props.fabricCompositionItems ?? [],
			pricing: props.pricing,
			notes: props.notes,
			reserved: props.reserved ?? false,
		});
	}

	getEffectivePrice(): Money {
		return this.pricing.discountedPrice.orElse(this.pricing.price);
	}

	formatEffectivePrice(): string {
		const effective = this.getEffectivePrice();
		const euros = effective.amount / 100;
		return `${euros.toFixed(2).replace(".", ",")} €`;
	}

	formatPrice(): string {
		const euros = this.pricing.price.amount / 100;
		return `${euros.toFixed(2).replace(".", ",")} €`;
	}

	hasDiscount(): boolean {
		return this.pricing.discountedPrice.isSome();
	}

	getLowestPriceInLast30Days(): Option<Money> {
		if (!this.hasDiscount()) {
			return none();
		}

		const thirtyDaysAgo = new Date();
		thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

		const recentPrices = this.pricing.priceHistory
			.filter((entry) => entry.timestamp >= thirtyDaysAgo)
			.map((entry) => entry.price);

		recentPrices.push(this.pricing.price);

		return someOrNone(recentPrices.reduce((lowest, current) => (current.amount < lowest.amount ? current : lowest)));
	}

	formatLowestPriceInLast30Days(): Option<string> {
		return this.getLowestPriceInLast30Days().map((lowest) => {
			const euros = lowest.amount / 100;
			return `${euros.toFixed(2).replace(".", ",")} €`;
		});
	}

	getFirstImage(): string | null {
		return this.images.length > 0 ? this.images[0] : null;
	}
}
