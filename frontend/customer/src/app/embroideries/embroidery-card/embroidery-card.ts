import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import { NgOptimizedImage } from "@angular/common";
import { Card } from "primeng/card";
import { Embroidery } from "../embroidery";

@Component({
	selector: "app-embroidery-card",
	templateUrl: "./embroidery-card.html",
	styleUrl: "./embroidery-card.scss",
	standalone: true,
	imports: [Card, NgOptimizedImage],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmbroideryCard {
	@Input({ required: true })
	embroidery!: Embroidery;
}
