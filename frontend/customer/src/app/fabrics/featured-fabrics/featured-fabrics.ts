import { AsyncPipe, isPlatformBrowser } from "@angular/common";
import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit, PLATFORM_ID } from "@angular/core";
import { BehaviorSubject, map } from "rxjs";
import { Carousel } from "primeng/carousel";
import { SeedService } from "../../services/seed.service";
import { ShowMoreCard } from "../../shared/show-more-card/show-more-card";
import { Fabric } from "../fabric";
import { FabricCard } from "../fabric-card/fabric-card";
import { FabricsService } from "../fabrics.service";

export type FabricCarouselItem =
	| { type: "fabric"; fabric: Fabric }
	| { type: "showMore" };

@Component({
	selector: "app-featured-fabrics",
	templateUrl: "./featured-fabrics.html",
	styleUrl: "./featured-fabrics.scss",
	standalone: true,
	imports: [AsyncPipe, FabricCard, ShowMoreCard, Carousel],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeaturedFabrics implements OnInit, OnDestroy {
	private readonly platformId = inject(PLATFORM_ID);
	private readonly seedService = inject(SeedService);
	private readonly fabricsService = inject(FabricsService);
	private readonly fabrics$ = new BehaviorSubject<Fabric[]>([]);

	readonly items$ = this.fabrics$.pipe(
		map((fabrics) => [
			...fabrics.map(
				(fabric): FabricCarouselItem => ({ type: "fabric", fabric }),
			),
			{ type: "showMore" } as FabricCarouselItem,
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
		this.fabricsService.getFeaturedFabrics(seed).subscribe((fabrics) => {
			this.fabrics$.next(fabrics);
		});
	}

	ngOnDestroy(): void {
		this.mediaQuery?.removeEventListener("change", this.mediaQueryListener);
		this.showNavigators$.complete();
		this.fabrics$.complete();
	}
}
