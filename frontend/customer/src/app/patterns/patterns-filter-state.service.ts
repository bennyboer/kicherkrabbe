import { DestroyRef, inject, Injectable, PLATFORM_ID } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { isPlatformBrowser } from "@angular/common";
import { BehaviorSubject, skip } from "rxjs";
import { none, Option, some } from "@kicherkrabbe/shared";

const STORAGE_KEY = "patterns-filter-state";

interface StoredState {
	categoryIds: string[];
	sizes: number[];
	sortAscending: boolean;
}

@Injectable()
export class PatternsFilterState {
	private readonly platformId = inject(PLATFORM_ID);
	private readonly destroyRef = inject(DestroyRef);

	readonly selectedCategoryIds$: BehaviorSubject<string[]>;
	readonly selectedSizes$: BehaviorSubject<number[]>;
	readonly sortAscending$: BehaviorSubject<boolean>;

	constructor() {
		const stored = this.loadState();
		this.selectedCategoryIds$ = new BehaviorSubject<string[]>(
			stored.map((s) => s.categoryIds).orElse([])
		);
		this.selectedSizes$ = new BehaviorSubject<number[]>(
			stored.map((s) => s.sizes).orElse([])
		);
		this.sortAscending$ = new BehaviorSubject<boolean>(
			stored.map((s) => s.sortAscending).orElse(true)
		);

		this.selectedCategoryIds$
			.pipe(skip(1), takeUntilDestroyed(this.destroyRef))
			.subscribe(() => this.saveState());
		this.selectedSizes$
			.pipe(skip(1), takeUntilDestroyed(this.destroyRef))
			.subscribe(() => this.saveState());
		this.sortAscending$
			.pipe(skip(1), takeUntilDestroyed(this.destroyRef))
			.subscribe(() => this.saveState());
	}

	reset(): void {
		this.selectedCategoryIds$.next([]);
		this.selectedSizes$.next([]);
		this.sortAscending$.next(true);
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
				categoryIds: this.selectedCategoryIds$.value,
				sizes: this.selectedSizes$.value,
				sortAscending: this.sortAscending$.value,
			};
			sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state));
		} catch (e) {
			console.warn("Failed to save patterns filter state", e);
		}
	}
}
