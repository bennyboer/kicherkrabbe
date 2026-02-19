import { validateProps } from "@kicherkrabbe/shared";

export interface TypeAvailability {
	typeId: string;
	inStock: boolean;
}

export class Fabric {
	readonly id: string;
	readonly alias: string;
	readonly name: string;
	readonly imageId: string;
	readonly exampleImageIds: string[];
	readonly colorIds: string[];
	readonly topicIds: string[];
	readonly availability: TypeAvailability[];

	private constructor(props: {
		id: string;
		alias: string;
		name: string;
		imageId: string;
		exampleImageIds: string[];
		colorIds: string[];
		topicIds: string[];
		availability: TypeAvailability[];
	}) {
		validateProps(props);

		this.id = props.id;
		this.alias = props.alias;
		this.name = props.name;
		this.imageId = props.imageId;
		this.exampleImageIds = props.exampleImageIds;
		this.colorIds = props.colorIds;
		this.topicIds = props.topicIds;
		this.availability = props.availability;
	}

	static of(props: {
		id: string;
		alias: string;
		name: string;
		imageId: string;
		exampleImageIds?: string[];
		colorIds?: string[];
		topicIds?: string[];
		availability?: TypeAvailability[];
	}): Fabric {
		return new Fabric({
			id: props.id,
			alias: props.alias,
			name: props.name,
			imageId: props.imageId,
			exampleImageIds: props.exampleImageIds ?? [],
			colorIds: props.colorIds ?? [],
			topicIds: props.topicIds ?? [],
			availability: props.availability ?? [],
		});
	}

	isInStock(): boolean {
		return this.availability.some((a) => a.inStock);
	}
}
