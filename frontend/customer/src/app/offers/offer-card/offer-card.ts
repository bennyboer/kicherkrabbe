import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import { NgOptimizedImage } from "@angular/common";
import { Card } from "primeng/card";
import { Offer } from "../offer";

@Component({
	selector: "app-offer-card",
	templateUrl: "./offer-card.html",
	styleUrl: "./offer-card.scss",
	standalone: true,
	imports: [Card, NgOptimizedImage],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OfferCard {
	@Input({ required: true })
	offer!: Offer;

	get imageId(): string | null {
		return this.offer.getFirstImage();
	}
}
