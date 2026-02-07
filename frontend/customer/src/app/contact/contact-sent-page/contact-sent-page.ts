import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { RouterLink } from "@angular/router";
import { SeoService } from "../../services/seo.service";

@Component({
	selector: "app-contact-sent-page",
	templateUrl: "./contact-sent-page.html",
	styleUrl: "./contact-sent-page.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
	imports: [RouterLink],
})
export class ContactSentPage {
	private readonly seoService = inject(SeoService);

	constructor() {
		this.seoService.updateMetaTags({
			title: "Nachricht gesendet | Kicherkrabbe",
			noIndex: true,
		});
	}
}
