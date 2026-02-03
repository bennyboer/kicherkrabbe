import { AsyncPipe } from "@angular/common";
import { ChangeDetectionStrategy, Component, inject, OnDestroy, OnInit } from "@angular/core";
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
	imports: [AsyncPipe, PatternCard, ShowMoreCard, Carousel],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeaturedPatterns implements OnInit, OnDestroy {
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
		this.patternsService.getFeaturedPatterns(seed).subscribe((patterns) => {
			this.patterns$.next(patterns);
		});
	}

	ngOnDestroy(): void {
		this.patterns$.complete();
	}
}
