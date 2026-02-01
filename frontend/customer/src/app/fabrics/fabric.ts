export class Fabric {
	readonly id: string;
	readonly name: string;
	readonly imageId: string;

	private constructor(props: { id: string; name: string; imageId: string }) {
		this.id = props.id;
		this.name = props.name;
		this.imageId = props.imageId;
	}

	static of(props: { id: string; name: string; imageId: string }): Fabric {
		return new Fabric(props);
	}
}
