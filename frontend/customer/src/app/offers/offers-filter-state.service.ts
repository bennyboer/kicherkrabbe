import { DestroyRef, inject, Injectable, PLATFORM_ID } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { isPlatformBrowser } from "@angular/common";
import { BehaviorSubject, combineLatest, debounceTime, skip } from "rxjs";
import { none, Option, some } from "@kicherkrabbe/shared";

const STORAGE_KEY = "offers-filter-state";

interface StoredState {
	searchTerm: string;
	categoryIds: string[];
	sizes: string[];
	minPrice: number | null;
	maxPrice: number | null;
	sort: string;
}

@Injectable()
export class OffersFilterState {
	private readonly platformId = inject(PLATFORM_ID);
	private readonly destroyRef = inject(DestroyRef);

	readonly searchTerm$: BehaviorSubject<string>;
	readonly selectedCategoryIds$: BehaviorSubject<string[]>;
	readonly selectedSizes$: BehaviorSubject<string[]>;
	readonly minPrice$: BehaviorSubject<number | null>;
	readonly maxPrice$: BehaviorSubject<number | null>;
	readonly sort$: BehaviorSubject<string>;

	constructor() {
		const stored = this.loadState();
		this.searchTerm$ = new BehaviorSubject<string>(
			stored.map((s) => s.searchTerm ?? "").orElse(""),
		);
		this.selectedCategoryIds$ = new BehaviorSubject<string[]>(
			stored.map((s) => s.categoryIds).orElse([]),
		);
		this.selectedSizes$ = new BehaviorSubject<string[]>(
			stored.map((s) => s.sizes).orElse([]),
		);
		this.minPrice$ = new BehaviorSubject<number | null>(
			stored.map((s) => s.minPrice).orElse(null),
		);
		this.maxPrice$ = new BehaviorSubject<number | null>(
			stored.map((s) => s.maxPrice).orElse(null),
		);
		this.sort$ = new BehaviorSubject<string>(
			stored.map((s) => s.sort).orElse("newest"),
		);

		combineLatest([
			this.searchTerm$,
			this.selectedCategoryIds$,
			this.selectedSizes$,
			this.minPrice$,
			this.maxPrice$,
			this.sort$,
		])
			.pipe(skip(1), debounceTime(50), takeUntilDestroyed(this.destroyRef))
			.subscribe(() => this.saveState());
	}

	reset(): void {
		this.searchTerm$.next("");
		this.selectedCategoryIds$.next([]);
		this.selectedSizes$.next([]);
		this.minPrice$.next(null);
		this.maxPrice$.next(null);
		this.sort$.next("newest");
	}

	private loadState(): Option<StoredState> {
		if (!isPlatformBrowser(this.platformId)) return none();
		try {
			const stored = sessionStorage.getItem(STORAGE_KEY);
			return stored ? some(JSON.parse(stored)) : none();
		} catch {
			return none();
		}
	}

	private saveState(): void {
		if (!isPlatformBrowser(this.platformId)) return;
		try {
			const state: StoredState = {
				searchTerm: this.searchTerm$.value,
				categoryIds: this.selectedCategoryIds$.value,
				sizes: this.selectedSizes$.value,
				minPrice: this.minPrice$.value,
				maxPrice: this.maxPrice$.value,
				sort: this.sort$.value,
			};
			sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state));
		} catch (e) {
			console.warn("Failed to save offers filter state", e);
		}
	}
}
