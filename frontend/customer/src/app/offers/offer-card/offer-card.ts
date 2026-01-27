import { Component, Input } from "@angular/core";
import { Card } from "primeng/card";
import type { Offer } from "./offer";

@Component({
	selector: "app-offer-card",
	templateUrl: "./offer-card.html",
	styleUrl: "./offer-card.scss",
	standalone: true,
	imports: [Card],
})
export class OfferCard {
	@Input()
	offer!: Offer;
}
