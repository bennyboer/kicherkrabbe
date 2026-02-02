import { AsyncPipe } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { BehaviorSubject, Observable } from "rxjs";
import { Carousel } from "primeng/carousel";
import { Pattern } from "../pattern";
import { PatternCard } from "../pattern-card/pattern-card";
import { PatternsService } from "../patterns.service";

@Component({
	selector: "app-featured-patterns",
	templateUrl: "./featured-patterns.html",
	styleUrl: "./featured-patterns.scss",
	standalone: true,
	imports: [AsyncPipe, PatternCard, Carousel],
})
export class FeaturedPatterns implements OnInit {
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
		this.patternsService.getFeaturedPatterns().subscribe((patterns) => {
			this.patterns$.next(patterns);
		});
	}

	getPatterns(): Observable<Pattern[]> {
		return this.patterns$.asObservable();
	}
}
