import { validateProps } from "@kicherkrabbe/shared";
import { Money } from "./money";

export class PriceHistoryEntry {
	readonly price: Money;
	readonly timestamp: Date;

	private constructor(props: { price: Money; timestamp: Date }) {
		validateProps(props);

		this.price = props.price;
		this.timestamp = props.timestamp;
	}

	static of(props: { price: Money; timestamp: Date }): PriceHistoryEntry {
		return new PriceHistoryEntry({
			price: props.price,
			timestamp: props.timestamp,
		});
	}
}
