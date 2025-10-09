export class Offer {
	readonly title: string;
	readonly price: string; // TODO Should be some kind of money
	readonly size: string; // TODO Should be soem kind of measurement
	readonly image: string; // TODO Should be some kind of URL (image type?)

	private constructor(props: {
		title: string;
		price: string;
		size: string;
		image: string;
	}) {
		// TODO Check preconditions

		this.title = props.title;
		this.price = props.price;
		this.size = props.size;
		this.image = props.image;
	}

	static of(props: {
		title: string;
		price: string;
		size: string;
		image: string;
	}): Offer {
		return new Offer(props);
	}
}
