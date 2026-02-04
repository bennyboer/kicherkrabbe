import { ChangeDetectionStrategy, Component, inject, Input, OnDestroy } from "@angular/core";
import { AsyncPipe } from "@angular/common";
import { Router } from "@angular/router";
import { BehaviorSubject, map } from "rxjs";
import { Card } from "primeng/card";
import { Fabric } from "../fabric";
import { FabricsService } from "../fabrics.service";

@Component({
	selector: "app-fabric-card",
	templateUrl: "./fabric-card.html",
	styleUrl: "./fabric-card.scss",
	standalone: true,
	imports: [Card, AsyncPipe],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FabricCard implements OnDestroy {
	private readonly router = inject(Router);
	private readonly fabricsService = inject(FabricsService);
	private readonly fabric$ = new BehaviorSubject<Fabric | null>(null);

	readonly imageUrl$ = this.fabric$.pipe(
		map((fabric) => (fabric ? this.fabricsService.getImageUrl(fabric.imageId) : ""))
	);

	@Input()
	set fabric(value: Fabric) {
		this.fabric$.next(value);
	}

	get fabric(): Fabric {
		return this.fabric$.value!;
	}

	ngOnDestroy(): void {
		this.fabric$.complete();
	}

	navigateToDetails(): void {
		this.router.navigate(["/fabrics", this.fabric.id]);
	}
}
