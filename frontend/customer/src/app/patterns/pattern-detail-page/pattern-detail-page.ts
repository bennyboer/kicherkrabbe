import { AsyncPipe } from "@angular/common";
import {
	ChangeDetectionStrategy,
	Component,
	inject,
	type OnDestroy,
	type OnInit,
} from "@angular/core";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { MessageService } from "primeng/api";
import { Button } from "primeng/button";
import { Divider } from "primeng/divider";
import { Image } from "primeng/image";
import { ProgressSpinner } from "primeng/progressspinner";
import { QuillViewComponent } from "ngx-quill";
import { BehaviorSubject, Subject, switchMap, takeUntil } from "rxjs";
import { SeoService } from "../../services/seo.service";
import { Breadcrumbs, type BreadcrumbItem } from "../../shared";
import type { Pattern, PricedSizeRange } from "../pattern";
import { PatternsService } from "../patterns.service";

@Component({
	selector: "app-pattern-detail-page",
	templateUrl: "./pattern-detail-page.html",
	styleUrl: "./pattern-detail-page.scss",
	standalone: true,
	imports: [
		AsyncPipe,
		RouterLink,
		Button,
		ProgressSpinner,
		Divider,
		Image,
		QuillViewComponent,
		Breadcrumbs,
	],
	providers: [MessageService],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternDetailPage implements OnInit, OnDestroy {
	private readonly route = inject(ActivatedRoute);
	private readonly router = inject(Router);
	private readonly patternsService = inject(PatternsService);
	private readonly messageService = inject(MessageService);
	private readonly seoService = inject(SeoService);
	private readonly destroy$ = new Subject<void>();

	readonly pattern$ = new BehaviorSubject<Pattern | null>(null);
	readonly loading$ = new BehaviorSubject<boolean>(true);
	readonly selectedImageIndex$ = new BehaviorSubject<number>(0);
	readonly breadcrumbs$ = new BehaviorSubject<BreadcrumbItem[]>([]);

	ngOnInit(): void {
		this.route.paramMap
			.pipe(
				switchMap((params) => {
					const id = params.get("id");
					if (!id) {
						throw new Error("Pattern ID is required");
					}
					this.pattern$.next(null);
					this.loading$.next(true);
					return this.patternsService.getPattern(id);
				}),
				takeUntil(this.destroy$)
			)
			.subscribe({
				next: (pattern) => {
					this.pattern$.next(pattern);
					this.selectedImageIndex$.next(0);
					this.loading$.next(false);
					this.updateSeo(pattern);
				},
				error: () => {
					this.loading$.next(false);
					this.messageService.add({
						severity: "error",
						summary: "Fehler",
						detail: "Der Schnitt konnte nicht geladen werden.",
					});
				},
			});
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
		this.pattern$.complete();
		this.loading$.complete();
		this.selectedImageIndex$.complete();
		this.breadcrumbs$.complete();
		this.seoService.clearStructuredData();
	}

	getImageUrl(imageId: string): string {
		return this.patternsService.getImageUrl(imageId, 1536);
	}

	getOriginalImageUrl(imageId: string): string {
		return this.patternsService.getImageUrl(imageId);
	}

	getThumbnailUrl(imageId: string): string {
		return this.patternsService.getImageUrl(imageId, 384);
	}

	selectImage(index: number): void {
		this.selectedImageIndex$.next(index);
	}

	formatPrice(amount: number): string {
		const euros = amount / 100;
		return euros.toFixed(2).replace(".", ",") + " €";
	}

	formatSizeRange(range: PricedSizeRange): string {
		if (range.to === null || range.from === range.to) {
			return `${range.from}`;
		}
		return `${range.from} - ${range.to}`;
	}

	sortedSizeRanges(ranges: PricedSizeRange[]): PricedSizeRange[] {
		return [...ranges].sort((a, b) => a.from - b.from);
	}

	goBack(): void {
		this.router.navigate([".."], { relativeTo: this.route });
	}

	private updateSeo(pattern: Pattern): void {
		const canonicalPath = `/patterns/${pattern.alias}`;

		this.seoService.updateMetaTags({
			title: `${pattern.name} | Kicherkrabbe`,
			description: `${pattern.name} - Handgefertigte Kinderkleidung von Kicherkrabbe. Individuelle Schnitte für Babys und Kinder.`,
			canonical: `https://kicherkrabbe.com${canonicalPath}`,
		});

		if (pattern.images.length > 0) {
			this.seoService.setProductImage(pattern.images[0]);
		}

		this.breadcrumbs$.next([
			{ label: "Schnitte", url: "/patterns" },
			{ label: pattern.name },
		]);

		this.seoService.setBreadcrumbStructuredData([
			{ name: "Startseite", url: "/" },
			{ name: "Schnitte", url: "/patterns" },
			{ name: pattern.name, url: canonicalPath },
		]);

		const minPrice = pattern.getMinPrice();
		this.seoService.setProductStructuredData({
			name: pattern.name,
			description: `${pattern.name} - Handgefertigte Kinderkleidung von Kicherkrabbe`,
			image: pattern.images.length > 0 ? this.getImageUrl(pattern.images[0]) : undefined,
			sku: pattern.number ?? undefined,
			offers: minPrice
				? {
						price: minPrice.amount,
						priceCurrency: minPrice.currency,
						availability: "InStock",
					}
				: undefined,
		});
	}
}
