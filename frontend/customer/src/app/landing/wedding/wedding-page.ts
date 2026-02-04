import { ChangeDetectionStrategy, Component } from "@angular/core";
import { RouterLink } from "@angular/router";

@Component({
	selector: "app-wedding-page",
	templateUrl: "./wedding-page.html",
	styleUrl: "./wedding-page.scss",
	standalone: true,
	imports: [RouterLink],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WeddingPage {}
