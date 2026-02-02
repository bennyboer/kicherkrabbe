import { AsyncPipe } from "@angular/common";
import { Component, inject, OnInit } from "@angular/core";
import { BehaviorSubject, Observable, map } from "rxjs";
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
	imports: [AsyncPipe, PatternCard, ShowMoreCard, Carousel],
})
export class FeaturedPatterns implements OnInit {
	private readonly seedService = inject(SeedService);
	private readonly patterns$: BehaviorSubject<Pattern[]> = new BehaviorSubject<
		Pattern[]
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

	constructor(private readonly patternsService: PatternsService) {}

	ngOnInit(): void {
		const seed = this.seedService.getSeed();
		this.patternsService.getFeaturedPatterns(seed).subscribe((patterns) => {
			this.patterns$.next(patterns);
		});
	}

	getItems(): Observable<PatternCarouselItem[]> {
		return this.patterns$.pipe(
			map((patterns) => [
				...patterns.map(
					(pattern): PatternCarouselItem => ({ type: "pattern", pattern }),
				),
				{ type: "showMore" } as PatternCarouselItem,
			]),
		);
	}
}
