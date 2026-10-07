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
import { InputText } from "primeng/inputtext";
import { Embroidery } from "../embroidery";
import { EmbroideriesService } from "../embroideries.service";
import { EmbroideriesFilterState } from "../embroideries-filter-state.service";
import { Category } from "../model";
import { EmbroideryCard } from "../embroidery-card/embroidery-card";
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

const EMBROIDERIES_LIMIT = 50;

const arraysEqual = <T>(a: T[], b: T[]): boolean =>
	a.length === b.length && a.every((val, i) => val === b[i]);

@Component({
	selector: "app-embroideries-page",
	templateUrl: "./embroideries-page.html",
	styleUrl: "./embroideries-page.scss",
	standalone: true,
	imports: [
		AsyncPipe,
		FormsModule,
		RouterLink,
		MultiSelect,
		Button,
		ProgressSpinner,
		InputText,
		EmbroideryCard,
		FilterLayout,
	],
	providers: [MessageService],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmbroideriesPage implements OnInit, OnDestroy {
	private readonly embroideriesService = inject(EmbroideriesService);
	private readonly messageService = inject(MessageService);
	private readonly filterState = inject(EmbroideriesFilterState);
	private readonly seoService = inject(SeoService);
	private readonly destroy$ = new Subject<void>();

	constructor() {
		this.seoService.updateMetaTags({
			title: "Stickereien | Kicherkrabbe",
			description:
				"Entdecke unsere Stickereien für handgefertigte Kinderkleidung von Kicherkrabbe.",
			canonical: "https://kicherkrabbe.com/embroideries",
		});
	}

	readonly categories$ = new BehaviorSubject<CategoryOption[]>([]);
	readonly embroideries$ = new BehaviorSubject<Embroidery[]>([]);
	readonly total$ = new BehaviorSubject<number>(0);
	readonly loading$ = new BehaviorSubject<boolean>(true);

	readonly searchTerm$ = this.filterState.searchTerm$;
	readonly selectedCategoryIds$ = this.filterState.selectedCategoryIds$;
	readonly sort$ = this.filterState.sort$;

	readonly hasMore$ = combineLatest([this.embroideries$, this.total$]).pipe(
		map(([embroideries, total]) => embroideries.length < total),
	);

	readonly hasActiveFilters$ = combineLatest([
		this.searchTerm$,
		this.selectedCategoryIds$,
		this.sort$,
	]).pipe(
		map(
			([searchTerm, categoryIds, sort]) =>
				searchTerm.length > 0 || categoryIds.length > 0 || sort !== "alpha-asc",
		),
	);

	readonly sortOptions: SortOption[] = [
		{ label: "A-Z", value: "alpha-asc" },
		{ label: "Z-A", value: "alpha-desc" },
	];

	get selectedSort(): string {
		return this.sort$.value;
	}

	set selectedSort(value: string) {
		this.sort$.next(value);
	}

	ngOnInit(): void {
		this.loadFilterOptions();
		this.setupFilterSubscription();
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
		this.categories$.complete();
		this.embroideries$.complete();
		this.total$.complete();
		this.loading$.complete();
	}

	onCategoriesChange(ids: string[]): void {
		this.selectedCategoryIds$.next(ids ?? []);
	}

	onSortChange(value: string): void {
		this.selectedSort = value;
	}

	resetFilters(): void {
		this.filterState.reset();
	}

	loadMore(): void {
		const currentEmbroideries = this.embroideries$.value;

		this.embroideriesService
			.getEmbroideries({
				searchTerm: this.searchTerm$.value,
				categoryIds: this.selectedCategoryIds$.value,
				ascending: this.isAscending(),
				skip: currentEmbroideries.length,
				limit: EMBROIDERIES_LIMIT,
			})
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (result) => {
					this.embroideries$.next([...currentEmbroideries, ...result.embroideries]);
					this.total$.next(result.total);
				},
				error: () => {
					this.showError();
				},
			});
	}

	private loadFilterOptions(): void {
		this.embroideriesService
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
	}

	private setupFilterSubscription(): void {
		combineLatest([
			this.searchTerm$.pipe(distinctUntilChanged()),
			this.selectedCategoryIds$.pipe(distinctUntilChanged(arraysEqual)),
			this.sort$.pipe(distinctUntilChanged()),
		])
			.pipe(debounceTime(300), takeUntil(this.destroy$))
			.subscribe(() => {
				this.loadEmbroideries();
			});
	}

	private loadEmbroideries(): void {
		this.loading$.next(true);
		this.embroideries$.next([]);

		this.embroideriesService
			.getEmbroideries({
				searchTerm: this.searchTerm$.value,
				categoryIds: this.selectedCategoryIds$.value,
				ascending: this.isAscending(),
				skip: 0,
				limit: EMBROIDERIES_LIMIT,
			})
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (result) => {
					this.embroideries$.next(result.embroideries);
					this.total$.next(result.total);
					this.loading$.next(false);
				},
				error: () => {
					this.loading$.next(false);
					this.showError();
				},
			});
	}

	private isAscending(): boolean {
		return this.sort$.value !== "alpha-desc";
	}

	private showError(): void {
		this.messageService.add({
			severity: "error",
			summary: "Fehler",
			detail: "Die Stickereien konnten nicht geladen werden. Bitte versuchen Sie es erneut.",
		});
	}
}
