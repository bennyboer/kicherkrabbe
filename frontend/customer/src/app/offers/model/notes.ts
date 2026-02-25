import { someOrNone, type Option, validateProps } from "@kicherkrabbe/shared";

export class Notes {
	readonly description: string;
	readonly contains: Option<string>;
	readonly care: Option<string>;
	readonly safety: Option<string>;

	private constructor(props: {
		description: string;
		contains: Option<string>;
		care: Option<string>;
		safety: Option<string>;
	}) {
		validateProps(props);

		this.description = props.description;
		this.contains = props.contains;
		this.care = props.care;
		this.safety = props.safety;
	}

	static of(props: {
		description: string;
		contains?: string | null;
		care?: string | null;
		safety?: string | null;
	}): Notes {
		return new Notes({
			description: props.description,
			contains: someOrNone(props.contains),
			care: someOrNone(props.care),
			safety: someOrNone(props.safety),
		});
	}
}
