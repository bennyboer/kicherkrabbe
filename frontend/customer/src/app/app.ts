import { Component } from "@angular/core";
import { ButtonModule } from "primeng/button";
import { BackgroundComponent } from "./background/background";
import { Badges } from "./badges/badges";
import { CrabHelper } from "./crab-helper/crab-helper";
import { Header } from "./header/header";
import { HighlightsComponent } from "./highlights/highlights";
import { Offer, OfferCard } from "./offers";

@Component({
	selector: "app-root",
	imports: [
		ButtonModule,
		Header,
		HighlightsComponent,
		BackgroundComponent,
		CrabHelper,
		Badges,
		OfferCard,
	],
	templateUrl: "./app.html",
	styleUrl: "./app.scss",
	standalone: true,
})
export class App {
	exampleA = Offer.of({
		title: "Cardigan",
		price: "32,00€",
		size: "73",
		image: "/images/examples/A.jpg",
	});
	exampleB = Offer.of({
		title: "Beanie",
		price: "15,00€",
		size: "42 KU",
		image: "/images/examples/B.jpg",
	});
	exampleC = Offer.of({
		title: "Herzchen Sweater",
		price: "40,00€",
		size: "104",
		image: "/images/examples/C.jpg",
	});
	exampleD = Offer.of({
		title: "Kuschelweste",
		price: "24,00€",
		size: "74",
		image: "/images/examples/D.jpg",
	});

	toggleDarkMode(): void {
		const element = document.querySelector("html");
		element?.classList.toggle("kicherkrabbe-dark-mode");
	}
}
