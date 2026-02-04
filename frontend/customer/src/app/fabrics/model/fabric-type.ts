import { validateProps } from "@kicherkrabbe/shared";

export class FabricType {
	readonly id: string;
	readonly name: string;

	private constructor(props: { id: string; name: string }) {
		validateProps(props);

		this.id = props.id;
		this.name = props.name;
	}

	static of(props: { id: string; name: string }): FabricType {
		return new FabricType({
			id: props.id,
			name: props.name,
		});
	}
}
