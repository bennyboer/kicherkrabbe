import { DestroyRef, inject, Injectable, PLATFORM_ID } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { isPlatformBrowser } from "@angular/common";
import { BehaviorSubject, skip } from "rxjs";
import { none, Option, some } from "@kicherkrabbe/shared";

const STORAGE_KEY = "fabrics-filter-state";

interface StoredState {
	topicIds: string[];
	colorIds: string[];
	inStockOnly: boolean;
	sortAscending: boolean;
}

@Injectable()
export class FabricsFilterState {
	private readonly platformId = inject(PLATFORM_ID);
	private readonly destroyRef = inject(DestroyRef);

	readonly selectedTopicIds$: BehaviorSubject<string[]>;
	readonly selectedColorIds$: BehaviorSubject<string[]>;
	readonly inStockOnly$: BehaviorSubject<boolean>;
	readonly sortAscending$: BehaviorSubject<boolean>;

	constructor() {
		const stored = this.loadState();
		this.selectedTopicIds$ = new BehaviorSubject<string[]>(
			stored.map((s) => s.topicIds).orElse([])
		);
		this.selectedColorIds$ = new BehaviorSubject<string[]>(
			stored.map((s) => s.colorIds).orElse([])
		);
		this.inStockOnly$ = new BehaviorSubject<boolean>(
			stored.map((s) => s.inStockOnly).orElse(false)
		);
		this.sortAscending$ = new BehaviorSubject<boolean>(
			stored.map((s) => s.sortAscending).orElse(true)
		);

		this.selectedTopicIds$
			.pipe(skip(1), takeUntilDestroyed(this.destroyRef))
			.subscribe(() => this.saveState());
		this.selectedColorIds$
			.pipe(skip(1), takeUntilDestroyed(this.destroyRef))
			.subscribe(() => this.saveState());
		this.inStockOnly$
			.pipe(skip(1), takeUntilDestroyed(this.destroyRef))
			.subscribe(() => this.saveState());
		this.sortAscending$
			.pipe(skip(1), takeUntilDestroyed(this.destroyRef))
			.subscribe(() => this.saveState());
	}

	reset(): void {
		this.selectedTopicIds$.next([]);
		this.selectedColorIds$.next([]);
		this.inStockOnly$.next(false);
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
				topicIds: this.selectedTopicIds$.value,
				colorIds: this.selectedColorIds$.value,
				inStockOnly: this.inStockOnly$.value,
				sortAscending: this.sortAscending$.value,
			};
			sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state));
		} catch (e) {
			console.warn("Failed to save fabrics filter state", e);
		}
	}
}
