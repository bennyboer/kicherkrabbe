import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { SeoService } from "../../services/seo.service";

@Component({
	selector: "app-warranty",
	templateUrl: "./warranty.html",
	styleUrls: ["../legal.scss", "./warranty.scss"],
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
})
export class WarrantyPage {
	private readonly seoService = inject(SeoService);

	constructor() {
		this.seoService.updateMetaTags({
			title: "Gesetzliche Gewährleistung | Kicherkrabbe",
			canonical: "https://kicherkrabbe.com/legal/warranty",
		});
	}
}
