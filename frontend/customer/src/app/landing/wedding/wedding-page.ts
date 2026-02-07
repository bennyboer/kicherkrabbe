import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { RouterLink } from "@angular/router";
import { Button } from "primeng/button";
import { SeoService } from "../../services/seo.service";

@Component({
	selector: "app-wedding-page",
	templateUrl: "./wedding-page.html",
	styleUrl: "./wedding-page.scss",
	standalone: true,
	imports: [RouterLink, Button],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WeddingPage {
	private readonly seoService = inject(SeoService);

	constructor() {
		this.seoService.updateMetaTags({
			title: "Kinderkleidung für Hochzeiten | Kicherkrabbe",
			description:
				"Handgefertigte Kinder- und Babykleidung für Hochzeiten und festliche Anlässe. Einzigartige Outfits für kleine Gäste.",
			canonical: "https://kicherkrabbe.com/landing/hochzeit",
		});
	}
}
