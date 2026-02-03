import { ChangeDetectionStrategy, Component } from "@angular/core";

@Component({
	selector: "app-patterns-page",
	templateUrl: "./patterns-page.html",
	styleUrl: "./patterns-page.scss",
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternsPage {}
