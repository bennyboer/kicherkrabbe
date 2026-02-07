import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { SeoService } from "../../services/seo.service";

@Component({
	selector: "app-terms-and-conditions",
	templateUrl: "./terms-and-conditions.html",
	styleUrl: "../legal.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
})
export class TermsAndConditionsPage {
	private readonly seoService = inject(SeoService);

	constructor() {
		this.seoService.updateMetaTags({
			title: "AGB | Kicherkrabbe",
			canonical: "https://kicherkrabbe.com/legal/terms-and-conditions",
		});
	}
}
