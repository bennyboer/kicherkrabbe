import { validateProps } from "@kicherkrabbe/shared";

export class FabricCompositionItem {
	readonly fabricType: string;
	readonly percentage: number;

	private constructor(props: { fabricType: string; percentage: number }) {
		validateProps(props);

		this.fabricType = props.fabricType;
		this.percentage = props.percentage;
	}

	static of(props: { fabricType: string; percentage: number }): FabricCompositionItem {
		return new FabricCompositionItem({
			fabricType: props.fabricType,
			percentage: props.percentage,
		});
	}
}
