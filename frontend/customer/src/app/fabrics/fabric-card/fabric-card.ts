import { Component, Input } from "@angular/core";
import { Card } from "primeng/card";
import { Fabric } from "../fabric";
import { FabricsService } from "../fabrics.service";

@Component({
	selector: "app-fabric-card",
	templateUrl: "./fabric-card.html",
	styleUrl: "./fabric-card.scss",
	standalone: true,
	imports: [Card],
})
export class FabricCard {
	@Input()
	fabric!: Fabric;

	constructor(private readonly fabricsService: FabricsService) {}

	getImageUrl(): string {
		return this.fabricsService.getImageUrl(this.fabric.imageId);
	}
}
