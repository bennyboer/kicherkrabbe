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
import { Slider } from "primeng/slider";
import { Offer } from "../offer";
import { OffersService } from "../offers.service";
import { OffersFilterState } from "../offers-filter-state.service";
import { Category } from "../model";
import { OfferCard } from "../offer-card/offer-card";
import { FilterLayout } from "../../shared";
import { SeoService } from "../../services/seo.service";

interface CategoryOption {
	id: string;
	name: string;
}

interface SizeOption {
	value: string;
	label: string;
}

interface SortOption {
	label: string;
	value: string;
}

const OFFERS_LIMIT = 50;
const PRICE_SLIDER_MIN = 0;
const PRICE_SLIDER_MAX = 300;

const arraysEqual = <T>(a: T[], b: T[]): boolean =>
	a.length === b.length && a.every((val, i) => val === b[i]);

@Component({
	selector: "app-offers-page",
	templateUrl: "./offers-page.html",
	styleUrl: "./offers-page.scss",
	standalone: true,
	imports: [
		AsyncPipe,
		FormsModule,
		RouterLink,
		MultiSelect,
		Button,
		ProgressSpinner,
		InputText,
		Slider,
		OfferCard,
		FilterLayout,
	],
	providers: [MessageService],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OffersPage implements OnInit, OnDestroy {
	private readonly offersService = inject(OffersService);
	private readonly messageService = inject(MessageService);
	private readonly filterState = inject(OffersFilterState);
	private readonly seoService = inject(SeoService);
	private readonly destroy$ = new Subject<void>();

	constructor() {
		this.seoService.updateMetaTags({
			title: "Sofortkäufe | Kicherkrabbe",
			description:
				"Entdecke unsere sofort verfügbaren, handgefertigten Kinderkleidungsstücke. Individuelle Unikate direkt zum Kaufen.",
			canonical: "https://kicherkrabbe.com/offers",
		});
	}

	readonly categories$ = new BehaviorSubject<CategoryOption[]>([]);
	readonly sizes$ = new BehaviorSubject<SizeOption[]>([]);
	readonly offers$ = new BehaviorSubject<Offer[]>([]);
	readonly total$ = new BehaviorSubject<number>(0);
	readonly loading$ = new BehaviorSubject<boolean>(true);

	readonly searchTerm$ = this.filterState.searchTerm$;
	readonly selectedCategoryIds$ = this.filterState.selectedCategoryIds$;
	readonly selectedSizes$ = this.filterState.selectedSizes$;
	readonly minPrice$ = this.filterState.minPrice$;
	readonly maxPrice$ = this.filterState.maxPrice$;
	readonly sort$ = this.filterState.sort$;

	readonly hasMore$ = combineLatest([this.offers$, this.total$]).pipe(
		map(([offers, total]) => offers.length < total),
	);

	readonly hasActiveFilters$ = combineLatest([
		this.searchTerm$,
		this.selectedCategoryIds$,
		this.selectedSizes$,
		this.minPrice$,
		this.maxPrice$,
		this.sort$,
	]).pipe(
		map(
			([searchTerm, categoryIds, sizes, minPrice, maxPrice, sort]) =>
				searchTerm.length > 0 ||
				categoryIds.length > 0 ||
				sizes.length > 0 ||
				minPrice !== null ||
				maxPrice !== null ||
				sort !== "newest",
		),
	);

	readonly sortOptions: SortOption[] = [
		{ label: "Neueste zuerst", value: "newest" },
		{ label: "A-Z", value: "alpha-asc" },
		{ label: "Z-A", value: "alpha-desc" },
		{ label: "Preis aufsteigend", value: "price-asc" },
		{ label: "Preis absteigend", value: "price-desc" },
	];

	readonly priceSliderMin = PRICE_SLIDER_MIN;
	readonly priceSliderMax = PRICE_SLIDER_MAX;

	readonly priceRange$ = combineLatest([this.minPrice$, this.maxPrice$]).pipe(
		map(([min, max]): [number, number] => [
			min !== null ? min / 100 : PRICE_SLIDER_MIN,
			max !== null ? max / 100 : PRICE_SLIDER_MAX,
		]),
	);

	get selectedSort(): string {
		return this.sort$.value;
	}

	set selectedSort(value: string) {
		this.sort$.next(value);
	}

	onPriceRangeChange(value: [number, number]): void {
		this.minPrice$.next(value[0] > PRICE_SLIDER_MIN ? Math.round(value[0] * 100) : null);
		this.maxPrice$.next(value[1] < PRICE_SLIDER_MAX ? Math.round(value[1] * 100) : null);
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
		this.offers$.complete();
		this.total$.complete();
		this.loading$.complete();
	}

	onCategoriesChange(ids: string[]): void {
		this.selectedCategoryIds$.next(ids);
	}

	onSizesChange(sizes: string[]): void {
		this.selectedSizes$.next(sizes);
	}

	onSortChange(value: string): void {
		this.selectedSort = value;
	}

	resetFilters(): void {
		this.filterState.reset();
	}

	loadMore(): void {
		const currentOffers = this.offers$.value;
		const skip = currentOffers.length;

		this.offersService
			.getOffers({
				searchTerm: this.searchTerm$.value,
				categoryIds: this.selectedCategoryIds$.value,
				sizes: this.selectedSizes$.value,
				priceRange: this.buildPriceRange(),
				sort: this.buildSort(),
				skip,
				limit: OFFERS_LIMIT,
			})
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (result) => {
					this.offers$.next([...currentOffers, ...result.offers]);
					this.total$.next(result.total);
				},
				error: () => {
					this.showError();
				},
			});
	}

	private loadFilterOptions(): void {
		this.offersService
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

		this.offersService
			.getAvailableSizes()
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (sizes) => {
					this.sizes$.next(sizes.map((s) => ({ value: s, label: s })));
				},
				error: (err) => {
					if (err.status !== 0) console.error("Failed to load sizes", err);
				},
			});
	}

	private setupFilterSubscription(): void {
		combineLatest([
			this.searchTerm$.pipe(distinctUntilChanged()),
			this.selectedCategoryIds$.pipe(distinctUntilChanged(arraysEqual)),
			this.selectedSizes$.pipe(distinctUntilChanged(arraysEqual)),
			this.minPrice$.pipe(distinctUntilChanged()),
			this.maxPrice$.pipe(distinctUntilChanged()),
			this.sort$.pipe(distinctUntilChanged()),
		])
			.pipe(debounceTime(300), takeUntil(this.destroy$))
			.subscribe(() => {
				this.loadOffers();
			});
	}

	private loadOffers(): void {
		this.loading$.next(true);
		this.offers$.next([]);

		this.offersService
			.getOffers({
				searchTerm: this.searchTerm$.value,
				categoryIds: this.selectedCategoryIds$.value,
				sizes: this.selectedSizes$.value,
				priceRange: this.buildPriceRange(),
				sort: this.buildSort(),
				skip: 0,
				limit: OFFERS_LIMIT,
			})
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (result) => {
					this.offers$.next(result.offers);
					this.total$.next(result.total);
					this.loading$.next(false);
				},
				error: () => {
					this.loading$.next(false);
					this.showError();
				},
			});
	}

	private buildPriceRange(): { minPrice: number | null; maxPrice: number | null } | null {
		const minPrice = this.minPrice$.value;
		const maxPrice = this.maxPrice$.value;
		if (minPrice === null && maxPrice === null) return null;
		return { minPrice, maxPrice };
	}

	private buildSort(): { property: "NEWEST" | "ALPHABETICAL" | "PRICE"; direction: "ASCENDING" | "DESCENDING" } {
		switch (this.sort$.value) {
			case "alpha-asc":
				return { property: "ALPHABETICAL", direction: "ASCENDING" };
			case "alpha-desc":
				return { property: "ALPHABETICAL", direction: "DESCENDING" };
			case "price-asc":
				return { property: "PRICE", direction: "ASCENDING" };
			case "price-desc":
				return { property: "PRICE", direction: "DESCENDING" };
			default:
				return { property: "NEWEST", direction: "DESCENDING" };
		}
	}

	private showError(): void {
		this.messageService.add({
			severity: "error",
			summary: "Fehler",
			detail: "Die Sofortkäufe konnten nicht geladen werden. Bitte versuchen Sie es erneut.",
		});
	}
}
