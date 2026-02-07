import { DOCUMENT } from "@angular/common";
import { Injectable, inject } from "@angular/core";
import { Meta, Title } from "@angular/platform-browser";

export interface SeoConfig {
	title?: string;
	description?: string;
	keywords?: string;
	ogTitle?: string;
	ogDescription?: string;
	ogImage?: string;
	ogUrl?: string;
	canonical?: string;
	noIndex?: boolean;
}

export interface BreadcrumbStructuredDataItem {
	name: string;
	url: string;
}

export interface ProductStructuredData {
	name: string;
	description?: string;
	image?: string;
	sku?: string;
	brand?: string;
	offers?: {
		price: number;
		priceCurrency: string;
		availability: "InStock" | "OutOfStock" | "PreOrder";
	};
}

const SITE_URL = "https://kicherkrabbe.com";
const API_URL = "https://api.kicherkrabbe.com";
const DEFAULT_TITLE = "Kicherkrabbe | Handmade Kinderkleidung aus Bayern";
const DEFAULT_DESCRIPTION =
	"Handgefertigte Kinderkleidung aus Bayern. Individuelle Kleidungsstücke für Babys und Kinder in Größen 56-116.";
const DEFAULT_OG_IMAGE = `${SITE_URL}/images/og-default.jpg`;

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
		this.updateOrCreateMeta(
			"og:description",
			config.ogDescription ?? description,
			"property",
		);
		this.updateOrCreateMeta(
			"og:image",
			config.ogImage ?? DEFAULT_OG_IMAGE,
			"property",
		);

		if (config.ogUrl) {
			this.updateOrCreateMeta("og:url", config.ogUrl, "property");
		}

		if (config.canonical) {
			this.updateCanonicalLink(config.canonical);
			this.updateOrCreateMeta("og:url", config.canonical, "property");
		}

		if (config.noIndex) {
			this.updateOrCreateMeta("robots", "noindex, nofollow");
		} else {
			this.removeMetaTag("robots");
		}
	}

	setCanonical(path: string): void {
		const url = path.startsWith("http") ? path : `${SITE_URL}${path}`;
		this.updateCanonicalLink(url);
		this.updateOrCreateMeta("og:url", url, "property");
	}

	setProductImage(imageId: string): void {
		const imageUrl = `${API_URL}/assets/${imageId}/content`;
		this.updateOrCreateMeta("og:image", imageUrl, "property");
	}

	setOrganizationStructuredData(): void {
		const data = {
			"@context": "https://schema.org",
			"@type": "Organization",
			name: "Kicherkrabbe",
			url: SITE_URL,
			logo: `${SITE_URL}/images/logo.svg`,
			description: DEFAULT_DESCRIPTION,
			address: {
				"@type": "PostalAddress",
				streetAddress: "Weststr. 23 ½",
				addressLocality: "Taufkirchen (Vils)",
				postalCode: "84416",
				addressCountry: "DE",
			},
			contactPoint: {
				"@type": "ContactPoint",
				email: "info@kicherkrabbe.com",
				contactType: "customer service",
			},
		};
		this.setStructuredData("organization", data);
	}

	setProductStructuredData(product: ProductStructuredData): void {
		const data: Record<string, unknown> = {
			"@context": "https://schema.org",
			"@type": "Product",
			name: product.name,
			brand: {
				"@type": "Brand",
				name: product.brand ?? "Kicherkrabbe",
			},
		};

		if (product.description) {
			data["description"] = product.description;
		}

		if (product.image) {
			data["image"] = product.image;
		}

		if (product.sku) {
			data["sku"] = product.sku;
		}

		if (product.offers) {
			data["offers"] = {
				"@type": "Offer",
				price: (product.offers.price / 100).toFixed(2),
				priceCurrency: product.offers.priceCurrency,
				availability: `https://schema.org/${product.offers.availability}`,
				seller: {
					"@type": "Organization",
					name: "Kicherkrabbe",
				},
			};
		}

		this.setStructuredData("product", data);
	}

	setBreadcrumbStructuredData(items: BreadcrumbStructuredDataItem[]): void {
		const data = {
			"@context": "https://schema.org",
			"@type": "BreadcrumbList",
			itemListElement: items.map((item, index) => ({
				"@type": "ListItem",
				position: index + 1,
				name: item.name,
				item: item.url.startsWith("http") ? item.url : `${SITE_URL}${item.url}`,
			})),
		};
		this.setStructuredData("breadcrumb", data);
	}

	clearStructuredData(): void {
		const scripts = this.document.querySelectorAll(
			'script[type="application/ld+json"]',
		);

		for (const script of scripts) {
			script.remove();
		}
	}

	resetToDefaults(): void {
		this.updateMetaTags({
			title: DEFAULT_TITLE,
			description: DEFAULT_DESCRIPTION,
		});
		this.removeCanonicalLink();
		this.clearStructuredData();
	}

	private setStructuredData(id: string, data: Record<string, unknown>): void {
		const existingScript = this.document.querySelector(
			`script[data-sd-id="${id}"]`,
		);
		if (existingScript) {
			existingScript.textContent = JSON.stringify(data);
			return;
		}

		const script = this.document.createElement("script");
		script.type = "application/ld+json";
		script.setAttribute("data-sd-id", id);
		script.textContent = JSON.stringify(data);
		this.document.head.appendChild(script);
	}

	private updateOrCreateMeta(
		name: string,
		content: string,
		attr: "name" | "property" = "name",
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
		let link: HTMLLinkElement | null = this.document.querySelector(
			'link[rel="canonical"]',
		);

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

	private removeMetaTag(name: string): void {
		this.meta.removeTag(`name="${name}"`);
	}
}
