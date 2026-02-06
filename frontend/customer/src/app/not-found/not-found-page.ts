import { ChangeDetectionStrategy, Component } from "@angular/core";
import { RouterLink } from "@angular/router";

@Component({
	selector: "app-not-found-page",
	templateUrl: "./not-found-page.html",
	styleUrl: "./not-found-page.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
	imports: [RouterLink],
})
export class NotFoundPage {}
