import { ChangeDetectionStrategy, Component } from "@angular/core";

@Component({
	selector: "app-background",
	templateUrl: "./background.html",
	styleUrl: "./background.scss",
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BackgroundComponent {}
