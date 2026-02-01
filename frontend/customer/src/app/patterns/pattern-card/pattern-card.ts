import { Component, Input } from "@angular/core";
import { Card } from "primeng/card";
import { Pattern } from "../pattern";
import { PatternsService } from "../patterns.service";

@Component({
	selector: "app-pattern-card",
	templateUrl: "./pattern-card.html",
	styleUrl: "./pattern-card.scss",
	standalone: true,
	imports: [Card],
})
export class PatternCard {
	@Input()
	pattern!: Pattern;

	constructor(private readonly patternsService: PatternsService) {}

	getImageUrl(): string {
		const imageId = this.pattern.getFirstImage();
		if (imageId === null) {
			return "";
		}
		return this.patternsService.getImageUrl(imageId);
	}
}
