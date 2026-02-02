import { AsyncPipe } from "@angular/common";
import { Component, inject, OnInit } from "@angular/core";
import { BehaviorSubject, Observable } from "rxjs";
import { Carousel } from "primeng/carousel";
import { SeedService } from "../../services/seed.service";
import { Fabric } from "../fabric";
import { FabricCard } from "../fabric-card/fabric-card";
import { FabricsService } from "../fabrics.service";

@Component({
	selector: "app-featured-fabrics",
	templateUrl: "./featured-fabrics.html",
	styleUrl: "./featured-fabrics.scss",
	standalone: true,
	imports: [AsyncPipe, FabricCard, Carousel],
})
export class FeaturedFabrics implements OnInit {
	private readonly seedService = inject(SeedService);
	private readonly fabrics$: BehaviorSubject<Fabric[]> = new BehaviorSubject<
		Fabric[]
	>([]);

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

	constructor(private readonly fabricsService: FabricsService) {}

	ngOnInit(): void {
		const seed = this.seedService.getSeed();
		this.fabricsService.getFeaturedFabrics(seed).subscribe((fabrics) => {
			this.fabrics$.next(fabrics);
		});
	}

	getFabrics(): Observable<Fabric[]> {
		return this.fabrics$.asObservable();
	}
}
