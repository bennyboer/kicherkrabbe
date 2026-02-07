import {
	ChangeDetectionStrategy,
	Component,
	inject,
	OnDestroy,
	OnInit,
} from "@angular/core";
import { AsyncPipe } from "@angular/common";
import {
	BehaviorSubject,
	combineLatest,
	debounceTime,
	distinctUntilChanged,
	map,
	Subject,
	takeUntil,
} from "rxjs";
import { FormsModule } from "@angular/forms";
import { RouterLink } from "@angular/router";
import { MessageService } from "primeng/api";
import { MultiSelect } from "primeng/multiselect";
import { Button } from "primeng/button";
import { ProgressSpinner } from "primeng/progressspinner";
import { Pattern } from "../pattern";
import { PatternsService, SizeOption } from "../patterns.service";
import { PatternsFilterState } from "../patterns-filter-state.service";
import { Category } from "../model";
import { PatternCard } from "../pattern-card/pattern-card";
import { FilterLayout } from "../../shared";
import { SeoService } from "../../services/seo.service";

interface CategoryOption {
	id: string;
	name: string;
}

interface SortOption {
	label: string;
	value: string;
}

const PATTERNS_LIMIT = 50;

const arraysEqual = <T>(a: T[], b: T[]): boolean =>
	a.length === b.length && a.every((val, i) => val === b[i]);

@Component({
	selector: "app-patterns-page",
	templateUrl: "./patterns-page.html",
	styleUrl: "./patterns-page.scss",
	standalone: true,
	imports: [
		AsyncPipe,
		FormsModule,
		RouterLink,
		MultiSelect,
		Button,
		ProgressSpinner,
		PatternCard,
		FilterLayout,
	],
	providers: [MessageService],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternsPage implements OnInit, OnDestroy {
	private readonly patternsService = inject(PatternsService);
	private readonly messageService = inject(MessageService);
	private readonly filterState = inject(PatternsFilterState);
	private readonly seoService = inject(SeoService);
	private readonly destroy$ = new Subject<void>();

	constructor() {
		this.seoService.updateMetaTags({
			title: "Schnitte | Kicherkrabbe",
			description:
				"Entdecke unsere handgefertigten Schnittmuster für Kinderkleidung. Individuelle Designs für Babys und Kinder.",
		});
	}

	readonly categories$ = new BehaviorSubject<CategoryOption[]>([]);
	readonly sizes$ = new BehaviorSubject<SizeOption[]>([]);
	readonly patterns$ = new BehaviorSubject<Pattern[]>([]);
	readonly total$ = new BehaviorSubject<number>(0);
	readonly loading$ = new BehaviorSubject<boolean>(true);

	readonly selectedCategoryIds$ = this.filterState.selectedCategoryIds$;
	readonly selectedSizes$ = this.filterState.selectedSizes$;
	readonly sortAscending$ = this.filterState.sortAscending$;

	readonly hasMore$ = combineLatest([this.patterns$, this.total$]).pipe(
		map(([patterns, total]) => patterns.length < total)
	);

	readonly hasActiveFilters$ = combineLatest([
		this.selectedCategoryIds$,
		this.selectedSizes$,
		this.sortAscending$,
	]).pipe(
		map(
			([categoryIds, sizes, sortAscending]) =>
				categoryIds.length > 0 || sizes.length > 0 || !sortAscending
		)
	);

	readonly sortOptions: SortOption[] = [
		{ label: "A-Z", value: "asc" },
		{ label: "Z-A", value: "desc" },
	];

	get selectedSort(): string {
		return this.sortAscending$.value ? "asc" : "desc";
	}

	set selectedSort(value: string) {
		this.sortAscending$.next(value === "asc");
	}

	ngOnInit(): void {
		this.loadFilterOptions();
		this.setupFilterSubscription();
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
		this.categories$.complete();
		this.sizes$.complete();
		this.patterns$.complete();
		this.total$.complete();
		this.loading$.complete();
	}

	onCategoriesChange(ids: string[]): void {
		this.selectedCategoryIds$.next(ids);
	}

	onSizesChange(sizes: number[]): void {
		this.selectedSizes$.next(sizes);
	}

	onSortChange(value: string): void {
		this.selectedSort = value;
	}

	resetFilters(): void {
		this.filterState.reset();
	}

	loadMore(): void {
		const currentPatterns = this.patterns$.value;
		const skip = currentPatterns.length;

		this.patternsService
			.getPatterns({
				categoryIds: this.selectedCategoryIds$.value,
				sizes: this.selectedSizes$.value,
				sortAscending: this.sortAscending$.value,
				skip,
				limit: PATTERNS_LIMIT,
			})
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (result) => {
					this.patterns$.next([...currentPatterns, ...result.patterns]);
					this.total$.next(result.total);
				},
				error: () => {
					this.showError();
				},
			});
	}

	private loadFilterOptions(): void {
		this.patternsService
			.getAvailableCategories()
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (categories) => {
					const sorted = categories
						.map((c: Category) => ({ id: c.id, name: c.name }))
						.sort((a, b) => a.name.localeCompare(b.name));
					this.categories$.next(sorted);
				},
				error: (err) => {
					if (err.status !== 0) console.error("Failed to load categories", err);
				},
			});

		this.sizes$.next(this.patternsService.getAvailableSizes());
	}

	private setupFilterSubscription(): void {
		combineLatest([
			this.selectedCategoryIds$.pipe(distinctUntilChanged(arraysEqual)),
			this.selectedSizes$.pipe(distinctUntilChanged(arraysEqual)),
			this.sortAscending$.pipe(distinctUntilChanged()),
		])
			.pipe(debounceTime(100), takeUntil(this.destroy$))
			.subscribe(([categoryIds, sizes, sortAscending]) => {
				this.loadPatterns(categoryIds, sizes, sortAscending);
			});
	}

	private loadPatterns(
		categoryIds: string[],
		sizes: number[],
		sortAscending: boolean
	): void {
		this.loading$.next(true);
		this.patterns$.next([]);

		this.patternsService
			.getPatterns({
				categoryIds,
				sizes,
				sortAscending,
				skip: 0,
				limit: PATTERNS_LIMIT,
			})
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (result) => {
					this.patterns$.next(result.patterns);
					this.total$.next(result.total);
					this.loading$.next(false);
				},
				error: () => {
					this.loading$.next(false);
					this.showError();
				},
			});
	}

	private showError(): void {
		this.messageService.add({
			severity: "error",
			summary: "Fehler",
			detail: "Die Schnitte konnten nicht geladen werden. Bitte versuchen Sie es erneut.",
		});
	}
}
