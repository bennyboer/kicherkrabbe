import { AsyncPipe, isPlatformBrowser } from "@angular/common";
import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit, PLATFORM_ID } from "@angular/core";
import { RouterLink } from "@angular/router";
import { BehaviorSubject, map, Subject, takeUntil } from "rxjs";
import { Carousel } from "primeng/carousel";
import { SeedService } from "../../services/seed.service";
import { ShowMoreCard } from "../../shared/show-more-card/show-more-card";
import { Embroidery } from "../embroidery";
import { EmbroideryCard } from "../embroidery-card/embroidery-card";
import { EmbroideriesService } from "../embroideries.service";

export type EmbroideryCarouselItem =
	| { type: "embroidery"; embroidery: Embroidery }
	| { type: "showMore" };

@Component({
	selector: "app-featured-embroideries",
	templateUrl: "./featured-embroideries.html",
	styleUrl: "./featured-embroideries.scss",
	standalone: true,
	imports: [AsyncPipe, RouterLink, EmbroideryCard, ShowMoreCard, Carousel],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeaturedEmbroideries implements OnInit, OnDestroy {
	private readonly platformId = inject(PLATFORM_ID);
	private readonly embroideriesService = inject(EmbroideriesService);
	private readonly seedService = inject(SeedService);
	private readonly destroy$ = new Subject<void>();
	private readonly embroideries$ = new BehaviorSubject<Embroidery[]>([]);

	readonly items$ = this.embroideries$.pipe(
		map((embroideries) => [
			...embroideries.map(
				(embroidery): EmbroideryCarouselItem => ({ type: "embroidery", embroidery }),
			),
			{ type: "showMore" } as EmbroideryCarouselItem,
		]),
	);

	private mediaQuery: MediaQueryList | null = null;
	private readonly mediaQueryListener = (e: MediaQueryListEvent) => this.showNavigators$.next(e.matches);

	readonly showNavigators$ = new BehaviorSubject(true);

	responsiveOptions = [
		{
			breakpoint: "1400px",
			numVisible: 4,
			numScroll: 1,
		},
		{
			breakpoint: "1024px",
			numVisible: 3,
			numScroll: 1,
		},
		{
			breakpoint: "768px",
			numVisible: 2,
			numScroll: 1,
		},
		{
			breakpoint: "560px",
			numVisible: 1,
			numScroll: 1,
		},
	];

	ngOnInit(): void {
		if (isPlatformBrowser(this.platformId)) {
			this.mediaQuery = window.matchMedia("(min-width: 769px)");
			this.showNavigators$.next(this.mediaQuery.matches);
			this.mediaQuery.addEventListener("change", this.mediaQueryListener);
		}

		const seed = this.seedService.getSeed();
		this.embroideriesService
			.getFeaturedEmbroideries(seed)
			.pipe(takeUntil(this.destroy$))
			.subscribe((embroideries) => {
				this.embroideries$.next(embroideries);
			});
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
		this.mediaQuery?.removeEventListener("change", this.mediaQueryListener);
		this.showNavigators$.complete();
		this.embroideries$.complete();
	}
}
