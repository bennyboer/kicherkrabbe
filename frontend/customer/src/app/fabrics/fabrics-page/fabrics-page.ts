import { AsyncPipe } from '@angular/common';
import {
	ChangeDetectionStrategy,
	Component,
	inject,
	type OnDestroy,
	type OnInit,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { MultiSelect } from 'primeng/multiselect';
import { ProgressSpinner } from 'primeng/progressspinner';
import { ToggleSwitch } from 'primeng/toggleswitch';
import {
	BehaviorSubject,
	combineLatest,
	debounceTime,
	distinctUntilChanged,
	map,
	Subject,
	takeUntil,
} from 'rxjs';
import { SeoService } from '../../services/seo.service';
import { ColorSwatch, FilterLayout } from '../../shared';
import type { Fabric } from '../fabric';
import { FabricCard } from '../fabric-card/fabric-card';
import { FabricsService } from '../fabrics.service';
import { FabricsFilterState } from '../fabrics-filter-state.service';
import type { Color, Topic } from '../model';

interface TopicOption {
	id: string;
	name: string;
}

interface ColorOption {
	id: string;
	name: string;
	red: number;
	green: number;
	blue: number;
}

interface SortOption {
	label: string;
	value: string;
}

const FABRICS_LIMIT = 50;

const arraysEqual = <T>(a: T[], b: T[]): boolean =>
	a.length === b.length && a.every((val, i) => val === b[i]);

@Component({
	selector: 'app-fabrics-page',
	templateUrl: './fabrics-page.html',
	styleUrl: './fabrics-page.scss',
	standalone: true,
	imports: [
		AsyncPipe,
		FormsModule,
		RouterLink,
		MultiSelect,
		ToggleSwitch,
		Button,
		ProgressSpinner,
		FabricCard,
		ColorSwatch,
		FilterLayout,
	],
	providers: [MessageService],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FabricsPage implements OnInit, OnDestroy {
	private readonly fabricsService = inject(FabricsService);
	private readonly messageService = inject(MessageService);
	private readonly filterState = inject(FabricsFilterState);
	private readonly seoService = inject(SeoService);
	private readonly destroy$ = new Subject<void>();

	constructor() {
		this.seoService.updateMetaTags({
			title: 'Stoffe | Kicherkrabbe',
			description: 'Entdecke unsere Stoffe mit individuellen Designs.',
			canonical: 'https://kicherkrabbe.com/fabrics',
		});
	}

	readonly topics$ = new BehaviorSubject<TopicOption[]>([]);
	readonly colors$ = new BehaviorSubject<ColorOption[]>([]);
	readonly fabrics$ = new BehaviorSubject<Fabric[]>([]);
	readonly total$ = new BehaviorSubject<number>(0);
	readonly loading$ = new BehaviorSubject<boolean>(true);

	readonly selectedTopicIds$ = this.filterState.selectedTopicIds$;
	readonly selectedColorIds$ = this.filterState.selectedColorIds$;
	readonly inStockOnly$ = this.filterState.inStockOnly$;
	readonly sortAscending$ = this.filterState.sortAscending$;

	readonly hasMore$ = combineLatest([this.fabrics$, this.total$]).pipe(
		map(([fabrics, total]) => fabrics.length < total),
	);

	readonly hasActiveFilters$ = combineLatest([
		this.selectedTopicIds$,
		this.selectedColorIds$,
		this.inStockOnly$,
		this.sortAscending$,
	]).pipe(
		map(
			([topicIds, colorIds, inStockOnly, sortAscending]) =>
				topicIds.length > 0 || colorIds.length > 0 || inStockOnly || !sortAscending,
		),
	);

	readonly sortOptions: SortOption[] = [
		{ label: 'A-Z', value: 'asc' },
		{ label: 'Z-A', value: 'desc' },
	];

	get selectedSort(): string {
		return this.sortAscending$.value ? 'asc' : 'desc';
	}

	set selectedSort(value: string) {
		this.sortAscending$.next(value === 'asc');
	}

	ngOnInit(): void {
		this.loadFilterOptions();
		this.setupFilterSubscription();
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
		this.topics$.complete();
		this.colors$.complete();
		this.fabrics$.complete();
		this.total$.complete();
		this.loading$.complete();
	}

	onTopicsChange(ids: string[]): void {
		this.selectedTopicIds$.next(ids ?? []);
	}

	onColorsChange(ids: string[]): void {
		this.selectedColorIds$.next(ids ?? []);
	}

	onInStockChange(value: boolean): void {
		this.inStockOnly$.next(value);
	}

	onSortChange(value: string): void {
		this.selectedSort = value;
	}

	resetFilters(): void {
		this.filterState.reset();
	}

	loadMore(): void {
		const currentFabrics = this.fabrics$.value;
		const skip = currentFabrics.length;

		this.fabricsService
			.getFabrics({
				topicIds: this.selectedTopicIds$.value,
				colorIds: this.selectedColorIds$.value,
				inStockOnly: this.inStockOnly$.value || undefined,
				sortAscending: this.sortAscending$.value,
				skip,
				limit: FABRICS_LIMIT,
			})
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (result) => {
					this.fabrics$.next([...currentFabrics, ...result.fabrics]);
					this.total$.next(result.total);
				},
				error: () => {
					this.showError();
				},
			});
	}

	private loadFilterOptions(): void {
		this.fabricsService
			.getAvailableTopics()
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (topics) => {
					const sorted = topics
						.map((t: Topic) => ({ id: t.id, name: t.name }))
						.sort((a, b) => a.name.localeCompare(b.name));
					this.topics$.next(sorted);
				},
				error: (err) => {
					if (err.status !== 0) console.error('Failed to load topics', err);
				},
			});

		this.fabricsService
			.getAvailableColors()
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (colors) => {
					const sorted = colors
						.map((c: Color) => ({
							id: c.id,
							name: c.name,
							red: c.red,
							green: c.green,
							blue: c.blue,
						}))
						.sort((a, b) => a.name.localeCompare(b.name));
					this.colors$.next(sorted);
				},
				error: (err) => {
					if (err.status !== 0) console.error('Failed to load colors', err);
				},
			});
	}

	private setupFilterSubscription(): void {
		combineLatest([
			this.selectedTopicIds$.pipe(distinctUntilChanged(arraysEqual)),
			this.selectedColorIds$.pipe(distinctUntilChanged(arraysEqual)),
			this.inStockOnly$.pipe(distinctUntilChanged()),
			this.sortAscending$.pipe(distinctUntilChanged()),
		])
			.pipe(debounceTime(100), takeUntil(this.destroy$))
			.subscribe(([topicIds, colorIds, inStockOnly, sortAscending]) => {
				this.loadFabrics(topicIds, colorIds, inStockOnly, sortAscending);
			});
	}

	private loadFabrics(
		topicIds: string[],
		colorIds: string[],
		inStockOnly: boolean,
		sortAscending: boolean,
	): void {
		this.loading$.next(true);
		this.fabrics$.next([]);

		this.fabricsService
			.getFabrics({
				topicIds,
				colorIds,
				inStockOnly: inStockOnly || undefined,
				sortAscending,
				skip: 0,
				limit: FABRICS_LIMIT,
			})
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (result) => {
					this.fabrics$.next(result.fabrics);
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
			severity: 'error',
			summary: 'Fehler',
			detail: 'Die Stoffe konnten nicht geladen werden. Bitte versuchen Sie es erneut.',
		});
	}
}
