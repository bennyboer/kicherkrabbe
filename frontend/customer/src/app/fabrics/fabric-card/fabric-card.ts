import { ChangeDetectionStrategy, Component, inject, Input } from "@angular/core";
import { NgOptimizedImage } from "@angular/common";
import { Router } from "@angular/router";
import { Card } from "primeng/card";
import { Fabric } from "../fabric";

@Component({
	selector: "app-fabric-card",
	templateUrl: "./fabric-card.html",
	styleUrl: "./fabric-card.scss",
	standalone: true,
	imports: [Card, NgOptimizedImage],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FabricCard {
	private readonly router = inject(Router);

	@Input({ required: true })
	fabric!: Fabric;

	navigateToDetails(): void {
		this.router.navigate(["/fabrics", this.fabric.alias]);
	}
}
