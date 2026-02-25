import { validateProps } from "@kicherkrabbe/shared";
import type {
	FabricCompositionItem,
	Money,
	Notes,
	OfferLink,
	Pricing,
} from "./model";

export class Offer {
	readonly id: string;
	readonly title: string;
	readonly size: string;
	readonly categoryIds: string[];
	readonly images: string[];
	readonly links: OfferLink[];
	readonly fabricCompositionItems: FabricCompositionItem[];
	readonly pricing: Pricing;
	readonly notes: Notes;

	private constructor(props: {
		id: string;
		title: string;
		size: string;
		categoryIds: string[];
		images: string[];
		links: OfferLink[];
		fabricCompositionItems: FabricCompositionItem[];
		pricing: Pricing;
		notes: Notes;
	}) {
		validateProps(props);

		this.id = props.id;
		this.title = props.title;
		this.size = props.size;
		this.categoryIds = props.categoryIds;
		this.images = props.images;
		this.links = props.links;
		this.fabricCompositionItems = props.fabricCompositionItems;
		this.pricing = props.pricing;
		this.notes = props.notes;
	}

	static of(props: {
		id: string;
		title: string;
		size: string;
		categoryIds?: string[];
		images: string[];
		links?: OfferLink[];
		fabricCompositionItems?: FabricCompositionItem[];
		pricing: Pricing;
		notes: Notes;
	}): Offer {
		return new Offer({
			id: props.id,
			title: props.title,
			size: props.size,
			categoryIds: props.categoryIds ?? [],
			images: props.images,
			links: props.links ?? [],
			fabricCompositionItems: props.fabricCompositionItems ?? [],
			pricing: props.pricing,
			notes: props.notes,
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

	getFirstImage(): string | null {
		return this.images.length > 0 ? this.images[0] : null;
	}
}
