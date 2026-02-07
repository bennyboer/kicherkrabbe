import { ChangeDetectionStrategy, Component } from "@angular/core";

@Component({
	selector: "app-badges",
	templateUrl: "./badges.html",
	styleUrls: ["./badges.scss"],
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Badges {
	badges = [
		{
			icon: "pi-heart-fill",
			title: "Echte Handarbeit",
		},
		{
			icon: "pi-map-marker",
			title: "Regional aus Bayern",
		},
		{
			icon: "pi-palette",
			title: "Eigens gedruckter Stoff",
		},
		{
			icon: "pi-star-fill",
			title: "Individuell, wie jedes Kind",
		},
	];
}
