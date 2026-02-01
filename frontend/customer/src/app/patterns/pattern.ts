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

export class Pattern {
	readonly id: string;
	readonly name: string;
	readonly images: string[];
	readonly variants: PatternVariant[];

	private constructor(props: {
		id: string;
		name: string;
		images: string[];
		variants: PatternVariant[];
	}) {
		this.id = props.id;
		this.name = props.name;
		this.images = props.images;
		this.variants = props.variants;
	}

	static of(props: {
		id: string;
		name: string;
		images: string[];
		variants: PatternVariant[];
	}): Pattern {
		return new Pattern(props);
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
}
