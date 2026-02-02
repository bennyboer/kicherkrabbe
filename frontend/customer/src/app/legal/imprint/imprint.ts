import { ChangeDetectionStrategy, Component } from "@angular/core";

@Component({
	selector: "app-imprint",
	templateUrl: "./imprint.html",
	styleUrl: "../legal.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
})
export class ImprintPage {}
