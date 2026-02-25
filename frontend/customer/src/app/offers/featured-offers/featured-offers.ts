import { AsyncPipe, isPlatformBrowser } from "@angular/common";
import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit, PLATFORM_ID } from "@angular/core";
import { RouterLink } from "@angular/router";
import { BehaviorSubject, map, Subject, takeUntil } from "rxjs";
import { Carousel } from "primeng/carousel";
import { ShowMoreCard } from "../../shared/show-more-card/show-more-card";
import { Offer } from "../offer";
import { OfferCard } from "../offer-card/offer-card";
import { OffersService } from "../offers.service";

export type OfferCarouselItem =
	| { type: "offer"; offer: Offer }
	| { type: "showMore" };

@Component({
	selector: "app-featured-offers",
	templateUrl: "./featured-offers.html",
	styleUrl: "./featured-offers.scss",
	standalone: true,
	imports: [AsyncPipe, RouterLink, OfferCard, ShowMoreCard, Carousel],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeaturedOffers implements OnInit, OnDestroy {
	private readonly platformId = inject(PLATFORM_ID);
	private readonly offersService = inject(OffersService);
	private readonly destroy$ = new Subject<void>();
	private readonly offers$ = new BehaviorSubject<Offer[]>([]);

	readonly items$ = this.offers$.pipe(
		map((offers) => [
			...offers.map(
				(offer): OfferCarouselItem => ({ type: "offer", offer }),
			),
			{ type: "showMore" } as OfferCarouselItem,
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

		this.offersService
			.getOffers({
				sort: { property: "NEWEST", direction: "DESCENDING" },
				limit: 8,
			})
			.pipe(takeUntil(this.destroy$))
			.subscribe((result) => {
				this.offers$.next(result.offers);
			});
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
		this.mediaQuery?.removeEventListener("change", this.mediaQueryListener);
		this.showNavigators$.complete();
		this.offers$.complete();
	}
}
