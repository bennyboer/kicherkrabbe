import { AsyncPipe, isPlatformBrowser } from "@angular/common";
import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit, PLATFORM_ID } from "@angular/core";
import { RouterLink } from "@angular/router";
import { BehaviorSubject, map } from "rxjs";
import { Carousel } from "primeng/carousel";
import { SeedService } from "../../services/seed.service";
import { ShowMoreCard } from "../../shared/show-more-card/show-more-card";
import { Pattern } from "../pattern";
import { PatternCard } from "../pattern-card/pattern-card";
import { PatternsService } from "../patterns.service";

export type PatternCarouselItem =
	| { type: "pattern"; pattern: Pattern }
	| { type: "showMore" };

@Component({
	selector: "app-featured-patterns",
	templateUrl: "./featured-patterns.html",
	styleUrl: "./featured-patterns.scss",
	standalone: true,
	imports: [AsyncPipe, RouterLink, PatternCard, ShowMoreCard, Carousel],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeaturedPatterns implements OnInit, OnDestroy {
	private readonly platformId = inject(PLATFORM_ID);
	private readonly seedService = inject(SeedService);
	private readonly patternsService = inject(PatternsService);
	private readonly patterns$ = new BehaviorSubject<Pattern[]>([]);

	readonly items$ = this.patterns$.pipe(
		map((patterns) => [
			...patterns.map(
				(pattern): PatternCarouselItem => ({ type: "pattern", pattern }),
			),
			{ type: "showMore" } as PatternCarouselItem,
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
		this.patternsService.getFeaturedPatterns(seed).subscribe((patterns) => {
			this.patterns$.next(patterns);
		});
	}

	ngOnDestroy(): void {
		this.mediaQuery?.removeEventListener("change", this.mediaQueryListener);
		this.showNavigators$.complete();
		this.patterns$.complete();
	}
}
