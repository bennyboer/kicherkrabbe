import {
	ChangeDetectionStrategy,
	Component,
	inject,
	OnDestroy,
	OnInit,
} from "@angular/core";
import { AsyncPipe } from "@angular/common";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { BehaviorSubject, Subject, switchMap, takeUntil } from "rxjs";
import { MessageService } from "primeng/api";
import { Button } from "primeng/button";
import { ProgressSpinner } from "primeng/progressspinner";
import { Divider } from "primeng/divider";
import { Image } from "primeng/image";
import { QuillViewComponent } from "ngx-quill";
import { Pattern, PricedSizeRange } from "../pattern";
import { PatternsService } from "../patterns.service";

@Component({
	selector: "app-pattern-detail-page",
	templateUrl: "./pattern-detail-page.html",
	styleUrl: "./pattern-detail-page.scss",
	standalone: true,
	imports: [
		AsyncPipe,
		RouterLink,
		Button,
		ProgressSpinner,
		Divider,
		Image,
		QuillViewComponent,
	],
	providers: [MessageService],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternDetailPage implements OnInit, OnDestroy {
	private readonly route = inject(ActivatedRoute);
	private readonly router = inject(Router);
	private readonly patternsService = inject(PatternsService);
	private readonly messageService = inject(MessageService);
	private readonly destroy$ = new Subject<void>();

	readonly pattern$ = new BehaviorSubject<Pattern | null>(null);
	readonly loading$ = new BehaviorSubject<boolean>(true);
	readonly selectedImageIndex$ = new BehaviorSubject<number>(0);

	ngOnInit(): void {
		this.route.paramMap
			.pipe(
				switchMap((params) => {
					const id = params.get("id");
					if (!id) {
						throw new Error("Pattern ID is required");
					}
					this.loading$.next(true);
					return this.patternsService.getPattern(id);
				}),
				takeUntil(this.destroy$)
			)
			.subscribe({
				next: (pattern) => {
					this.pattern$.next(pattern);
					this.selectedImageIndex$.next(0);
					this.loading$.next(false);
				},
				error: () => {
					this.loading$.next(false);
					this.messageService.add({
						severity: "error",
						summary: "Fehler",
						detail: "Der Schnitt konnte nicht geladen werden.",
					});
				},
			});
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
		this.pattern$.complete();
		this.loading$.complete();
		this.selectedImageIndex$.complete();
	}

	getImageUrl(imageId: string): string {
		return this.patternsService.getImageUrl(imageId);
	}

	selectImage(index: number): void {
		this.selectedImageIndex$.next(index);
	}

	formatPrice(amount: number): string {
		const euros = amount / 100;
		return euros.toFixed(2).replace(".", ",") + " \u20AC";
	}

	formatSizeRange(range: PricedSizeRange): string {
		if (range.to === null || range.from === range.to) {
			return `${range.from}`;
		}
		return `${range.from} - ${range.to}`;
	}

	sortedSizeRanges(ranges: PricedSizeRange[]): PricedSizeRange[] {
		return [...ranges].sort((a, b) => a.from - b.from);
	}

	goBack(): void {
		this.router.navigate([".."], { relativeTo: this.route });
	}
}
