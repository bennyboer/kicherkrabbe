import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { RouterLink } from "@angular/router";
import { SeoService } from "../services/seo.service";

@Component({
	selector: "app-not-found-page",
	templateUrl: "./not-found-page.html",
	styleUrl: "./not-found-page.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
	imports: [RouterLink],
})
export class NotFoundPage {
	private readonly seoService = inject(SeoService);

	constructor() {
		this.seoService.updateMetaTags({
			title: "Seite nicht gefunden | Kicherkrabbe",
		});
	}
}
