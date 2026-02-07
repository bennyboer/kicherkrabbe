import { Injectable, inject } from "@angular/core";
import { Meta, Title } from "@angular/platform-browser";
import { DOCUMENT } from "@angular/common";

export interface SeoConfig {
	title?: string;
	description?: string;
	keywords?: string;
	ogTitle?: string;
	ogDescription?: string;
	ogImage?: string;
	ogUrl?: string;
	canonical?: string;
}

const DEFAULT_TITLE = "Kicherkrabbe | Handmade Kinderkleidung aus Bayern";
const DEFAULT_DESCRIPTION =
	"Handgefertigte Kinderkleidung aus Bayern. Individuelle Kleidungsstücke für Babys und Kinder in Größen 56-116.";

@Injectable({
	providedIn: "root",
})
export class SeoService {
	private readonly meta = inject(Meta);
	private readonly title = inject(Title);
	private readonly document = inject(DOCUMENT);

	updateMetaTags(config: SeoConfig): void {
		const title = config.title ?? DEFAULT_TITLE;
		const description = config.description ?? DEFAULT_DESCRIPTION;

		this.title.setTitle(title);
		this.updateOrCreateMeta("description", description);

		if (config.keywords) {
			this.updateOrCreateMeta("keywords", config.keywords);
		}

		this.updateOrCreateMeta("og:title", config.ogTitle ?? title, "property");
		this.updateOrCreateMeta("og:description", config.ogDescription ?? description, "property");

		if (config.ogImage) {
			this.updateOrCreateMeta("og:image", config.ogImage, "property");
		}

		if (config.ogUrl) {
			this.updateOrCreateMeta("og:url", config.ogUrl, "property");
		}

		if (config.canonical) {
			this.updateCanonicalLink(config.canonical);
		}
	}

	resetToDefaults(): void {
		this.updateMetaTags({
			title: DEFAULT_TITLE,
			description: DEFAULT_DESCRIPTION,
		});
		this.removeCanonicalLink();
	}

	private updateOrCreateMeta(
		name: string,
		content: string,
		attr: "name" | "property" = "name"
	): void {
		const selector = attr === "name" ? `name="${name}"` : `property="${name}"`;
		const existing = this.meta.getTag(selector);

		if (existing) {
			this.meta.updateTag({ [attr]: name, content });
		} else {
			this.meta.addTag({ [attr]: name, content });
		}
	}

	private updateCanonicalLink(url: string): void {
		let link: HTMLLinkElement | null = this.document.querySelector('link[rel="canonical"]');

		if (link) {
			link.setAttribute("href", url);
		} else {
			link = this.document.createElement("link");
			link.setAttribute("rel", "canonical");
			link.setAttribute("href", url);
			this.document.head.appendChild(link);
		}
	}

	private removeCanonicalLink(): void {
		const link = this.document.querySelector('link[rel="canonical"]');
		if (link) {
			link.remove();
		}
	}
}
