import { DestroyRef, inject, Injectable, PLATFORM_ID } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { isPlatformBrowser } from "@angular/common";
import { BehaviorSubject, combineLatest, debounceTime, skip } from "rxjs";
import { none, Option, some } from "@kicherkrabbe/shared";

const STORAGE_KEY = "embroideries-filter-state";

interface StoredState {
	searchTerm: string;
	categoryIds: string[];
	sort: string;
}

@Injectable()
export class EmbroideriesFilterState {
	private readonly platformId = inject(PLATFORM_ID);
	private readonly destroyRef = inject(DestroyRef);

	readonly searchTerm$: BehaviorSubject<string>;
	readonly selectedCategoryIds$: BehaviorSubject<string[]>;
	readonly sort$: BehaviorSubject<string>;

	constructor() {
		const stored = this.loadState();
		this.searchTerm$ = new BehaviorSubject<string>(
			stored.map((s) => s.searchTerm ?? "").orElse(""),
		);
		this.selectedCategoryIds$ = new BehaviorSubject<string[]>(
			stored.map((s) => s.categoryIds).orElse([]),
		);
		this.sort$ = new BehaviorSubject<string>(
			stored.map((s) => s.sort).orElse("alpha-asc"),
		);

		combineLatest([this.searchTerm$, this.selectedCategoryIds$, this.sort$])
			.pipe(skip(1), debounceTime(50), takeUntilDestroyed(this.destroyRef))
			.subscribe(() => this.saveState());
	}

	reset(): void {
		this.searchTerm$.next("");
		this.selectedCategoryIds$.next([]);
		this.sort$.next("alpha-asc");
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
				sort: this.sort$.value,
			};
			sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state));
		} catch (e) {
			console.warn("Failed to save embroideries filter state", e);
		}
	}
}
