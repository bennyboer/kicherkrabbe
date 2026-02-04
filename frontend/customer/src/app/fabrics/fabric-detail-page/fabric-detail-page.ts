import {
	ChangeDetectionStrategy,
	Component,
	inject,
	OnDestroy,
	OnInit,
} from "@angular/core";
import { AsyncPipe } from "@angular/common";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import {
	BehaviorSubject,
	combineLatest,
	forkJoin,
	map,
	Subject,
	switchMap,
	takeUntil,
} from "rxjs";
import { MessageService } from "primeng/api";
import { Button } from "primeng/button";
import { ProgressSpinner } from "primeng/progressspinner";
import { Tag } from "primeng/tag";
import { Divider } from "primeng/divider";
import { Image } from "primeng/image";
import { Fabric } from "../fabric";
import { FabricsService } from "../fabrics.service";
import { Color, FabricType, Topic } from "../model";
import { ColorSwatch } from "../../shared";

@Component({
	selector: "app-fabric-detail-page",
	templateUrl: "./fabric-detail-page.html",
	styleUrl: "./fabric-detail-page.scss",
	standalone: true,
	imports: [
		AsyncPipe,
		RouterLink,
		Button,
		ProgressSpinner,
		Tag,
		Divider,
		Image,
		ColorSwatch,
	],
	providers: [MessageService],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FabricDetailPage implements OnInit, OnDestroy {
	private readonly route = inject(ActivatedRoute);
	private readonly router = inject(Router);
	private readonly fabricsService = inject(FabricsService);
	private readonly messageService = inject(MessageService);
	private readonly destroy$ = new Subject<void>();

	private readonly fabric$ = new BehaviorSubject<Fabric | null>(null);
	private readonly allColors$ = new BehaviorSubject<Color[]>([]);
	private readonly allTopics$ = new BehaviorSubject<Topic[]>([]);
	private readonly allFabricTypes$ = new BehaviorSubject<FabricType[]>([]);

	readonly loading$ = new BehaviorSubject<boolean>(true);

	readonly fabricData$ = combineLatest([
		this.fabric$,
		this.allColors$,
		this.allTopics$,
		this.allFabricTypes$,
	]).pipe(
		map(([fabric, allColors, allTopics, allFabricTypes]) => {
			if (!fabric) return null;

			const colors = allColors.filter((c) => fabric.colorIds.includes(c.id));
			const topics = allTopics.filter((t) => fabric.topicIds.includes(t.id));
			const fabricTypeAvailabilities = fabric.availability
				.map((a) => {
					const type = allFabricTypes.find((ft) => ft.id === a.typeId);
					return type ? { type, inStock: a.inStock } : null;
				})
				.filter(
					(ta): ta is { type: FabricType; inStock: boolean } => ta !== null
				);

			return { fabric, colors, topics, fabricTypeAvailabilities };
		})
	);

	ngOnInit(): void {
		this.loadMetadata();

		this.route.paramMap
			.pipe(
				switchMap((params) => {
					const id = params.get("id");
					if (!id) {
						throw new Error("Fabric ID is required");
					}
					this.loading$.next(true);
					return this.fabricsService.getFabric(id);
				}),
				takeUntil(this.destroy$)
			)
			.subscribe({
				next: (fabric) => {
					this.fabric$.next(fabric);
					this.loading$.next(false);
				},
				error: () => {
					this.loading$.next(false);
					this.messageService.add({
						severity: "error",
						summary: "Fehler",
						detail: "Der Stoff konnte nicht geladen werden.",
					});
				},
			});
	}

	ngOnDestroy(): void {
		this.destroy$.next();
		this.destroy$.complete();
		this.fabric$.complete();
		this.allColors$.complete();
		this.allTopics$.complete();
		this.allFabricTypes$.complete();
		this.loading$.complete();
	}

	getImageUrl(imageId: string): string {
		return this.fabricsService.getImageUrl(imageId);
	}

	goBack(): void {
		this.router.navigate([".."], { relativeTo: this.route });
	}

	private loadMetadata(): void {
		forkJoin([
			this.fabricsService.getAvailableColors(),
			this.fabricsService.getAvailableTopics(),
			this.fabricsService.getAvailableFabricTypes(),
		])
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: ([colors, topics, fabricTypes]) => {
					this.allColors$.next(colors);
					this.allTopics$.next(topics);
					this.allFabricTypes$.next(fabricTypes);
				},
				error: (err) => {
					if (err.status !== 0) console.error("Failed to load metadata", err);
				},
			});
	}
}
