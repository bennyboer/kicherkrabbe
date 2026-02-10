import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import { NgOptimizedImage } from "@angular/common";
import { Card } from "primeng/card";
import { Pattern } from "../pattern";

@Component({
	selector: "app-pattern-card",
	templateUrl: "./pattern-card.html",
	styleUrl: "./pattern-card.scss",
	standalone: true,
	imports: [Card, NgOptimizedImage],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternCard {
	@Input({ required: true })
	pattern!: Pattern;

	get imageId(): string | null {
		return this.pattern.getFirstImage();
	}
}
