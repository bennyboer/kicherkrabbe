import { Component } from "@angular/core";

@Component({
	selector: "app-badges",
	templateUrl: "./badges.html",
	styleUrls: ["./badges.scss"],
	standalone: true,
})
export class Badges {
	badges = [
		{
			icon: "✋",
			title: "Echte Handarbeit",
		},
		{
			icon: "🏔️",
			title: "Regional aus Bayern",
		},
		{
			icon: "🧵",
			title: "Eigens gedruckter Stoff",
		},
		{
			icon: "⭐",
			title: "Individuell, wie jedes Kind",
		},
	];
}
