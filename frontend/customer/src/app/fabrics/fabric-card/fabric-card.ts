import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import { NgOptimizedImage } from "@angular/common";
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
	@Input({ required: true })
	fabric!: Fabric;
}
