import { AsyncPipe } from "@angular/common";
import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit } from "@angular/core";
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
		const seed = this.seedService.getSeed();
		this.fabricsService.getFeaturedFabrics(seed).subscribe((fabrics) => {
			this.fabrics$.next(fabrics);
		});
	}

	ngOnDestroy(): void {
		this.fabrics$.complete();
	}
}
