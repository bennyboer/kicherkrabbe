import { ChangeDetectionStrategy, Component, inject, Input, OnDestroy } from "@angular/core";
import { AsyncPipe } from "@angular/common";
import { BehaviorSubject, map } from "rxjs";
import { Card } from "primeng/card";
import { Pattern } from "../pattern";
import { PatternsService } from "../patterns.service";

@Component({
	selector: "app-pattern-card",
	templateUrl: "./pattern-card.html",
	styleUrl: "./pattern-card.scss",
	standalone: true,
	imports: [Card, AsyncPipe],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternCard implements OnDestroy {
	private readonly patternsService = inject(PatternsService);
	private readonly pattern$ = new BehaviorSubject<Pattern | null>(null);

	readonly imageUrl$ = this.pattern$.pipe(
		map((pattern) => {
			if (!pattern) {
				return "";
			}
			const imageId = pattern.getFirstImage();
			if (imageId === null) {
				return "";
			}
			return this.patternsService.getImageUrl(imageId);
		})
	);

	@Input()
	set pattern(value: Pattern) {
		this.pattern$.next(value);
	}

	get pattern(): Pattern {
		return this.pattern$.value!;
	}

	ngOnDestroy(): void {
		this.pattern$.complete();
	}
}
