import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { SeoService } from "../../services/seo.service";

@Component({
	selector: "app-imprint",
	templateUrl: "./imprint.html",
	styleUrl: "../legal.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
})
export class ImprintPage {
	private readonly seoService = inject(SeoService);

	constructor() {
		this.seoService.updateMetaTags({
			title: "Impressum | Kicherkrabbe",
		});
	}
}
