import { Option, someOrNone, validateProps } from "@kicherkrabbe/shared";

export interface TypeAvailability {
	typeId: string;
	inStock: boolean;
}

export class Fabric {
	readonly id: string;
	readonly alias: string;
	readonly name: string;
	readonly kind: Option<string>;
	readonly imageId: Option<string>;
	readonly exampleImageIds: string[];
	readonly colorIds: string[];
	readonly topicIds: string[];
	readonly availability: TypeAvailability[];

	private constructor(props: {
		id: string;
		alias: string;
		name: string;
		kind: Option<string>;
		imageId: Option<string>;
		exampleImageIds: string[];
		colorIds: string[];
		topicIds: string[];
		availability: TypeAvailability[];
	}) {
		validateProps(props);

		this.id = props.id;
		this.alias = props.alias;
		this.name = props.name;
		this.kind = props.kind;
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
		kind?: string | null;
		imageId?: string | null;
		exampleImageIds?: string[];
		colorIds?: string[];
		topicIds?: string[];
		availability?: TypeAvailability[];
	}): Fabric {
		return new Fabric({
			id: props.id,
			alias: props.alias,
			name: props.name,
			kind: someOrNone(props.kind),
			imageId: someOrNone(props.imageId),
			exampleImageIds: props.exampleImageIds ?? [],
			colorIds: props.colorIds ?? [],
			topicIds: props.topicIds ?? [],
			availability: props.availability ?? [],
		});
	}

	isPatterned(): boolean {
		return this.kind.map((k) => k === "PATTERNED").orElse(true);
	}

	isSolidColor(): boolean {
		return this.kind.map((k) => k === "SOLID_COLOR").orElse(false);
	}

	isInStock(): boolean {
		return this.availability.some((a) => a.inStock);
	}

	getDisplayImageId(): string | null {
		if (this.imageId.isSome()) {
			return this.imageId.orElseThrow();
		}
		return this.exampleImageIds.length > 0 ? this.exampleImageIds[0] : null;
	}
}
