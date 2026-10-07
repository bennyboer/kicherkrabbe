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
import { Image } from "primeng/image";
import { ProgressSpinner } from "primeng/progressspinner";
import { BehaviorSubject, combineLatest, map, Subject, switchMap, takeUntil } from "rxjs";
import { SeoService } from "../../services/seo.service";
import { type BreadcrumbItem, Breadcrumbs } from "../../shared";
import type { Embroidery } from "../embroidery";
import { EmbroideriesService } from "../embroideries.service";
import type { Category } from "../model";

@Component({
	selector: "app-embroidery-detail-page",
	templateUrl: "./embroidery-detail-page.html",
	styleUrl: "./embroidery-detail-page.scss",
	standalone: true,
	imports: [AsyncPipe, RouterLink, Button, ProgressSpinner, Image, Breadcrumbs],
	providers: [MessageService],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmbroideryDetailPage implements OnInit, OnDestroy {
	private readonly route = inject(ActivatedRoute);
	private readonly router = inject(Router);
	private readonly embroideriesService = inject(EmbroideriesService);
	private readonly messageService = inject(MessageService);
	private readonly seoService = inject(SeoService);
	private readonly destroy$ = new Subject<void>();

	readonly state$ = new BehaviorSubject<{
		loading: boolean;
		embroidery: Embroidery | null;
	}>({
		loading: true,
		embroidery: null,
	});
	readonly categories$ = new BehaviorSubject<Category[]>([]);
	readonly breadcrumbs$ = new BehaviorSubject<BreadcrumbItem[]>([]);

	readonly categoryNames$ = combineLatest([this.state$, this.categories$]).pipe(
		map(([state, categories]) => {
			if (!state.embroidery) {
				return [];
			}

			const lookup = new Map(categories.map((c) => [c.id, c.name]));
			return state.embroidery.categoryIds
				.map((id) => lookup.get(id))
				.filter((name): name is string => !!name)
				.sort((a, b) => a.localeCompare(b));
		}),
	);

	ngOnInit(): void {
		this.embroideriesService
			.getAvailableCategories()
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (categories) => this.categories$.next(categories),
				error: (err) => {
					if (err.status !== 0) console.error("Failed to load categories", err);
				},
			});

		this.route.paramMap
			.pipe(
				switchMap((params) => {
					const id = params.get("id");
					if (!id) {
						throw new Error("Embroidery ID is required");
					}
					this.state$.next({ loading: true, embroidery: null });
					return this.embroideriesService.getEmbroidery(id);
				}),
				takeUntil(this.destroy$),
			)
			.subscribe({
				next: (embroidery) => {
					this.state$.next({ loading: false, embroidery });
					this.updateSeo(embroidery);
				},
				error: () => {
					this.state$.next({ loading: false, embroidery: null });
					this.messageService.add({
						severity: "error",
						summary: "Fehler",
						detail: "Die Stickerei konnte nicht geladen werden.",
					});
				},
			});
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
		this.state$.complete();
		this.categories$.complete();
		this.breadcrumbs$.complete();
		this.seoService.clearStructuredData();
	}

	getImageUrl(imageId: string): string {
		return this.embroideriesService.getImageUrl(imageId, 1536);
	}

	getOriginalImageUrl(imageId: string): string {
		return this.embroideriesService.getImageUrl(imageId);
	}

	goBack(): void {
		this.router.navigate([".."], { relativeTo: this.route });
	}

	private updateSeo(embroidery: Embroidery): void {
		const canonicalPath = `/embroideries/${embroidery.alias}`;

		this.seoService.updateMetaTags({
			title: `${embroidery.name} | Stickereien | Kicherkrabbe`,
			description: `Stickerei „${embroidery.name}“ für handgefertigte Kinderkleidung von Kicherkrabbe.`,
			canonical: `https://kicherkrabbe.com${canonicalPath}`,
		});

		this.seoService.setProductImage(embroidery.image);

		this.breadcrumbs$.next([
			{ label: "Stickereien", url: "/embroideries" },
			{ label: embroidery.name },
		]);

		this.seoService.setBreadcrumbStructuredData([
			{ name: "Startseite", url: "/" },
			{ name: "Stickereien", url: "/embroideries" },
			{ name: embroidery.name, url: canonicalPath },
		]);
	}
}
