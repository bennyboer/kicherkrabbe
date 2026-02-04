import { ChangeDetectionStrategy, Component } from "@angular/core";
import { RouterLink } from "@angular/router";

@Component({
	selector: "app-tradition-page",
	templateUrl: "./tradition-page.html",
	styleUrl: "./tradition-page.scss",
	standalone: true,
	imports: [RouterLink],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TraditionPage {}
