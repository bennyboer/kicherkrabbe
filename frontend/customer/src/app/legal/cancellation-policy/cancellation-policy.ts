import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { SeoService } from "../../services/seo.service";

@Component({
	selector: "app-cancellation-policy",
	templateUrl: "./cancellation-policy.html",
	styleUrl: "../legal.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
})
export class CancellationPolicyPage {
	private readonly seoService = inject(SeoService);

	constructor() {
		this.seoService.updateMetaTags({
			title: "Widerrufsbelehrung | Kicherkrabbe",
		});
	}
}
