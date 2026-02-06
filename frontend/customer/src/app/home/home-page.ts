import { DOCUMENT } from "@angular/common";
import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { RouterLink } from "@angular/router";
import { Badges } from "../badges/badges";
import { InquirySection } from "../contact";
import { CrabHelper } from "../crab-helper/crab-helper";
import { FeaturedFabrics } from "../fabrics";
import { HighlightsComponent } from "../highlights/highlights";
import { FeaturedPatterns } from "../patterns";

@Component({
	selector: "app-home-page",
	templateUrl: "./home-page.html",
	styleUrl: "./home-page.scss",
	standalone: true,
	imports: [
		RouterLink,
		HighlightsComponent,
		CrabHelper,
		Badges,
		FeaturedPatterns,
		FeaturedFabrics,
		InquirySection,
	],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomePage {
	private readonly document = inject(DOCUMENT);

	scrollToContactForm(): void {
		this.document.getElementById("contact-form")?.scrollIntoView({ behavior: "smooth" });
	}
}
