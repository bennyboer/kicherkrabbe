import { someOrNone, type Option, validateProps } from "@kicherkrabbe/shared";
import { Money } from "./money";
import { PriceHistoryEntry } from "./price-history-entry";

export class Pricing {
	readonly price: Money;
	readonly discountedPrice: Option<Money>;
	readonly priceHistory: PriceHistoryEntry[];

	private constructor(props: { price: Money; discountedPrice: Option<Money>; priceHistory: PriceHistoryEntry[] }) {
		validateProps(props);

		this.price = props.price;
		this.discountedPrice = props.discountedPrice;
		this.priceHistory = props.priceHistory;
	}

	static of(props: { price: Money; discountedPrice?: Money | null; priceHistory?: PriceHistoryEntry[] }): Pricing {
		return new Pricing({
			price: props.price,
			discountedPrice: someOrNone(props.discountedPrice),
			priceHistory: props.priceHistory ?? [],
		});
	}
}
