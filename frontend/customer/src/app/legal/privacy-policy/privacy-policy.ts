import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { SeoService } from "../../services/seo.service";

@Component({
	selector: "app-privacy-policy",
	templateUrl: "./privacy-policy.html",
	styleUrl: "../legal.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
})
export class PrivacyPolicyPage {
	private readonly seoService = inject(SeoService);

	constructor() {
		this.seoService.updateMetaTags({
			title: "Datenschutz | Kicherkrabbe",
			canonical: "https://kicherkrabbe.com/legal/privacy-policy",
		});
	}
}
