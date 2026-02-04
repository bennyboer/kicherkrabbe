import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import { Color } from "../../fabrics/model";

@Component({
	selector: "app-color-swatch",
	templateUrl: "./color-swatch.html",
	styleUrl: "./color-swatch.scss",
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ColorSwatch {
	@Input()
	color: Color | null = null;

	@Input()
	red: number = 0;

	@Input()
	green: number = 0;

	@Input()
	blue: number = 0;

	@Input()
	size: "small" | "medium" = "small";

	get backgroundColor(): string {
		if (this.color) {
			return this.color.toRgbString();
		}
		return `rgb(${this.red}, ${this.green}, ${this.blue})`;
	}
}
