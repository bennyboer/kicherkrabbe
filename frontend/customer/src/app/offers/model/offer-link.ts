import { validateProps } from "@kicherkrabbe/shared";

export class OfferLink {
	readonly type: string;
	readonly id: string;
	readonly name: string;

	private constructor(props: { type: string; id: string; name: string }) {
		validateProps(props);

		this.type = props.type;
		this.id = props.id;
		this.name = props.name;
	}

	static of(props: { type: string; id: string; name: string }): OfferLink {
		return new OfferLink({
			type: props.type,
			id: props.id,
			name: props.name,
		});
	}
}
