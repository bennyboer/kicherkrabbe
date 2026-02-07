import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { InquirySection } from "../inquiry-section/inquiry-section";
import { SeoService } from "../../services/seo.service";

@Component({
	selector: "app-contact-page",
	templateUrl: "./contact-page.html",
	styleUrl: "./contact-page.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
	imports: [InquirySection],
})
export class ContactPage {
	private readonly seoService = inject(SeoService);

	constructor() {
		this.seoService.updateMetaTags({
			title: "Kontakt | Kicherkrabbe",
			description:
				"Kontaktiere uns für individuelle Kinderkleidung. Wir beraten dich gerne zu deinem Wunschstück.",
		});
	}
}
