import { Component, Input } from "@angular/core";
import { RouterLink } from "@angular/router";

@Component({
	selector: "app-show-more-card",
	templateUrl: "./show-more-card.html",
	styleUrl: "./show-more-card.scss",
	standalone: true,
	imports: [RouterLink],
})
export class ShowMoreCard {
	@Input()
	label!: string;

	@Input()
	link!: string;
}
