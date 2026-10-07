import { validateProps } from "@kicherkrabbe/shared";

export class Embroidery {
	readonly id: string;
	readonly alias: string;
	readonly name: string;
	readonly image: string;
	readonly categoryIds: string[];

	private constructor(props: {
		id: string;
		alias: string;
		name: string;
		image: string;
		categoryIds: string[];
	}) {
		validateProps(props);

		this.id = props.id;
		this.alias = props.alias;
		this.name = props.name;
		this.image = props.image;
		this.categoryIds = props.categoryIds;
	}

	static of(props: {
		id: string;
		alias: string;
		name: string;
		image: string;
		categoryIds?: string[];
	}): Embroidery {
		return new Embroidery({
			id: props.id,
			alias: props.alias,
			name: props.name,
			image: props.image,
			categoryIds: props.categoryIds ?? [],
		});
	}
}
