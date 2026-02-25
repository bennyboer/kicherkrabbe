import { someOrNone, type Option, validateProps } from "@kicherkrabbe/shared";
import { Money } from "./money";

export class Pricing {
	readonly price: Money;
	readonly discountedPrice: Option<Money>;

	private constructor(props: { price: Money; discountedPrice: Option<Money> }) {
		validateProps(props);

		this.price = props.price;
		this.discountedPrice = props.discountedPrice;
	}

	static of(props: { price: Money; discountedPrice?: Money | null }): Pricing {
		return new Pricing({
			price: props.price,
			discountedPrice: someOrNone(props.discountedPrice),
		});
	}
}
