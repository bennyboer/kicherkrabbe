import { AsyncPipe } from "@angular/common";
import {
	ChangeDetectionStrategy,
	Component,
	inject,
	type OnDestroy,
	type OnInit,
} from "@angular/core";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { QuillViewComponent } from "ngx-quill";
import { MessageService } from "primeng/api";
import { Button } from "primeng/button";
import { Divider } from "primeng/divider";
import { Image } from "primeng/image";
import { Panel } from "primeng/panel";
import { ProgressSpinner } from "primeng/progressspinner";
import { BehaviorSubject, Subject, switchMap, takeUntil } from "rxjs";
import { SeoService } from "../../services/seo.service";
import { type BreadcrumbItem, Breadcrumbs } from "../../shared";
import type { OfferLink } from "../model";
import type { Offer } from "../offer";
import { OffersService } from "../offers.service";

const FABRIC_TYPE_LABELS: Record<string, string> = {
	COTTON: "Baumwolle",
	POLYESTER: "Polyester",
	ELASTANE: "Elasthan",
	VISCOSE: "Viskose",
	WOOL: "Wolle",
	SILK: "Seide",
	LINEN: "Leinen",
	NYLON: "Nylon",
	ACRYLIC: "Acryl",
	MODAL: "Modal",
	LYOCELL: "Lyocell",
	BAMBOO: "Bambus",
	HEMP: "Hanf",
	CASHMERE: "Kaschmir",
	RAYON: "Rayon",
};

const LINK_TYPE_LABELS: Record<string, string> = {
	PATTERN: "Schnitt",
	FABRIC: "Stoff",
};

@Component({
	selector: "app-offer-detail-page",
	templateUrl: "./offer-detail-page.html",
	styleUrl: "./offer-detail-page.scss",
	standalone: true,
	imports: [
		AsyncPipe,
		RouterLink,
		Button,
		ProgressSpinner,
		Divider,
		Image,
		Panel,
		QuillViewComponent,
		Breadcrumbs,
	],
	providers: [MessageService],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OfferDetailPage implements OnInit, OnDestroy {
	private readonly route = inject(ActivatedRoute);
	private readonly router = inject(Router);
	private readonly offersService = inject(OffersService);
	private readonly messageService = inject(MessageService);
	private readonly seoService = inject(SeoService);
	private readonly destroy$ = new Subject<void>();

	readonly state$ = new BehaviorSubject<{
		loading: boolean;
		offer: Offer | null;
	}>({
		loading: true,
		offer: null,
	});
	readonly selectedImageIndex$ = new BehaviorSubject<number>(0);
	readonly breadcrumbs$ = new BehaviorSubject<BreadcrumbItem[]>([]);

	ngOnInit(): void {
		this.route.paramMap
			.pipe(
				switchMap((params) => {
					const id = params.get("id");
					if (!id) {
						throw new Error("Offer ID is required");
					}
					this.state$.next({ loading: true, offer: null });
					return this.offersService.getOffer(id);
				}),
				takeUntil(this.destroy$),
			)
			.subscribe({
				next: (offer) => {
					this.state$.next({ loading: false, offer });
					this.selectedImageIndex$.next(0);
					this.updateSeo(offer);
				},
				error: () => {
					this.state$.next({ loading: false, offer: null });
					this.messageService.add({
						severity: "error",
						summary: "Fehler",
						detail: "Der Sofortkauf konnte nicht geladen werden.",
					});
				},
			});
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
		this.state$.complete();
		this.selectedImageIndex$.complete();
		this.breadcrumbs$.complete();
		this.seoService.clearStructuredData();
	}

	getImageUrl(imageId: string): string {
		return this.offersService.getImageUrl(imageId, 1536);
	}

	getOriginalImageUrl(imageId: string): string {
		return this.offersService.getImageUrl(imageId);
	}

	getThumbnailUrl(imageId: string): string {
		return this.offersService.getImageUrl(imageId, 384);
	}

	selectImage(index: number): void {
		this.selectedImageIndex$.next(index);
	}

	formatFabricType(fabricType: string): string {
		return FABRIC_TYPE_LABELS[fabricType] ?? fabricType;
	}

	formatPercentage(percentage: number): string {
		return (percentage / 100).toFixed(0) + "%";
	}

	getLinkRoute(link: OfferLink): string[] {
		switch (link.type) {
			case "PATTERN":
				return ["/patterns", link.id];
			case "FABRIC":
				return ["/fabrics", link.id];
			default:
				return [];
		}
	}

	getLinkTypeLabel(link: OfferLink): string {
		return LINK_TYPE_LABELS[link.type] ?? link.type;
	}

	goBack(): void {
		this.router.navigate([".."], { relativeTo: this.route });
	}

	private updateSeo(offer: Offer): void {
		const canonicalPath = `/offers/${offer.alias}`;

		this.seoService.updateMetaTags({
			title: `${offer.title} | Kicherkrabbe`,
			description: `${offer.title} (${offer.size}) - Handgefertigte Kinderkleidung von Kicherkrabbe. Sofort verfügbar.`,
			canonical: `https://kicherkrabbe.com${canonicalPath}`,
		});

		if (offer.images.length > 0) {
			this.seoService.setProductImage(offer.images[0]);
		}

		this.breadcrumbs$.next([
			{ label: "Sofortkäufe", url: "/offers" },
			{ label: offer.title },
		]);

		this.seoService.setBreadcrumbStructuredData([
			{ name: "Startseite", url: "/" },
			{ name: "Sofortkäufe", url: "/offers" },
			{ name: offer.title, url: canonicalPath },
		]);

		const effectivePrice = offer.getEffectivePrice();
		this.seoService.setProductStructuredData({
			name: offer.title,
			description: `${offer.title} (${offer.size}) - Handgefertigte Kinderkleidung von Kicherkrabbe`,
			image:
				offer.images.length > 0 ? this.getImageUrl(offer.images[0]) : undefined,
			offers: {
				price: effectivePrice.amount / 100,
				priceCurrency: effectivePrice.currency,
				availability: "InStock",
			},
		});
	}
}
