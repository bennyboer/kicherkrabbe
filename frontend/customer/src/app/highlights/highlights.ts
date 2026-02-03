import { ChangeDetectionStrategy, Component } from "@angular/core";

@Component({
	selector: "app-highlights",
	templateUrl: "./highlights.html",
	styleUrl: "./highlights.scss",
	standalone: true,
	imports: [],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HighlightsComponent {}
