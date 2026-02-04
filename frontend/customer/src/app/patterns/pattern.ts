import { validateProps } from "@kicherkrabbe/shared";

export interface Money {
	amount: number;
	currency: string;
}

export interface PricedSizeRange {
	from: number;
	to: number | null;
	unit: string | null;
	price: Money;
}

export interface PatternVariant {
	name: string;
	pricedSizeRanges: PricedSizeRange[];
}

export interface PatternExtra {
	name: string;
	price: Money;
}

export interface PatternAttribution {
	originalPatternName: string | null;
	designer: string | null;
}

export class Pattern {
	readonly id: string;
	readonly name: string;
	readonly number: string | null;
	readonly description: string | null;
	readonly attribution: PatternAttribution | null;
	readonly images: string[];
	readonly variants: PatternVariant[];
	readonly extras: PatternExtra[];
	readonly categoryIds: string[];

	private constructor(props: {
		id: string;
		name: string;
		number: string | null;
		description: string | null;
		attribution: PatternAttribution | null;
		images: string[];
		variants: PatternVariant[];
		extras: PatternExtra[];
		categoryIds: string[];
	}) {
		validateProps(props);

		this.id = props.id;
		this.name = props.name;
		this.number = props.number;
		this.description = props.description;
		this.attribution = props.attribution;
		this.images = props.images;
		this.variants = props.variants;
		this.extras = props.extras;
		this.categoryIds = props.categoryIds;
	}

	static of(props: {
		id: string;
		name: string;
		number?: string | null;
		description?: string | null;
		attribution?: PatternAttribution | null;
		images: string[];
		variants: PatternVariant[];
		extras?: PatternExtra[];
		categoryIds?: string[];
	}): Pattern {
		return new Pattern({
			id: props.id,
			name: props.name,
			number: props.number ?? null,
			description: props.description ?? null,
			attribution: props.attribution ?? null,
			images: props.images,
			variants: props.variants,
			extras: props.extras ?? [],
			categoryIds: props.categoryIds ?? [],
		});
	}

	getMinPrice(): Money | null {
		let minPrice: Money | null = null;

		for (const variant of this.variants) {
			for (const sizeRange of variant.pricedSizeRanges) {
				if (minPrice === null || sizeRange.price.amount < minPrice.amount) {
					minPrice = sizeRange.price;
				}
			}
		}

		return minPrice;
	}

	formatMinPrice(): string {
		const minPrice = this.getMinPrice();
		if (minPrice === null) {
			return "";
		}

		const euros = minPrice.amount / 100;
		return `ab ${euros.toFixed(2).replace(".", ",")} €`;
	}

	getFirstImage(): string | null {
		return this.images.length > 0 ? this.images[0] : null;
	}

	getAllSizes(): number[] {
		const sizes = new Set<number>();
		for (const variant of this.variants) {
			for (const range of variant.pricedSizeRanges) {
				sizes.add(range.from);
				if (range.to !== null) {
					sizes.add(range.to);
				}
			}
		}
		return Array.from(sizes).sort((a, b) => a - b);
	}
}
