import { validateProps } from "@kicherkrabbe/shared";

export interface TypeAvailability {
	typeId: string;
	inStock: boolean;
}

export class Fabric {
	readonly id: string;
	readonly name: string;
	readonly imageId: string;
	readonly colorIds: string[];
	readonly topicIds: string[];
	readonly availability: TypeAvailability[];

	private constructor(props: {
		id: string;
		name: string;
		imageId: string;
		colorIds: string[];
		topicIds: string[];
		availability: TypeAvailability[];
	}) {
		validateProps(props);

		this.id = props.id;
		this.name = props.name;
		this.imageId = props.imageId;
		this.colorIds = props.colorIds;
		this.topicIds = props.topicIds;
		this.availability = props.availability;
	}

	static of(props: {
		id: string;
		name: string;
		imageId: string;
		colorIds?: string[];
		topicIds?: string[];
		availability?: TypeAvailability[];
	}): Fabric {
		return new Fabric({
			id: props.id,
			name: props.name,
			imageId: props.imageId,
			colorIds: props.colorIds ?? [],
			topicIds: props.topicIds ?? [],
			availability: props.availability ?? [],
		});
	}

	isInStock(): boolean {
		return this.availability.some((a) => a.inStock);
	}
}
