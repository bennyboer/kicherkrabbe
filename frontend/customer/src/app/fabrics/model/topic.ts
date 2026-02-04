import { validateProps } from "@kicherkrabbe/shared";

export class Topic {
	readonly id: string;
	readonly name: string;

	private constructor(props: { id: string; name: string }) {
		validateProps(props);

		this.id = props.id;
		this.name = props.name;
	}

	static of(props: { id: string; name: string }): Topic {
		return new Topic({
			id: props.id,
			name: props.name,
		});
	}
}
