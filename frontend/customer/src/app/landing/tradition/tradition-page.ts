import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { RouterLink } from "@angular/router";
import { Button } from "primeng/button";
import { SeoService } from "../../services/seo.service";

@Component({
	selector: "app-tradition-page",
	templateUrl: "./tradition-page.html",
	styleUrl: "./tradition-page.scss",
	standalone: true,
	imports: [RouterLink, Button],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TraditionPage {
	private readonly seoService = inject(SeoService);

	constructor() {
		this.seoService.updateMetaTags({
			title: "Bayerische Tracht fuer Kinder | Kicherkrabbe",
			description:
				"Traditionelle bayerische Tracht für Kinder. Bequeme Dirndl und Lederhosen-Optik, handgemacht in Bayern.",
			canonical: "https://kicherkrabbe.com/landing/tracht",
		});
	}
}
