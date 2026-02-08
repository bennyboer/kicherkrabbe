import { ChangeDetectionStrategy, Component, inject, Input } from "@angular/core";
import { NgOptimizedImage } from "@angular/common";
import { Router } from "@angular/router";
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
	private readonly router = inject(Router);

	@Input({ required: true })
	pattern!: Pattern;

	get imageId(): string | null {
		return this.pattern.getFirstImage();
	}

	navigateToDetails(): void {
		this.router.navigate(["/patterns", this.pattern.alias]);
	}
}
