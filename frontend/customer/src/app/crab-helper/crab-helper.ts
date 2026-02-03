import { ChangeDetectionStrategy, Component } from "@angular/core";

@Component({
	selector: "app-crab-helper",
	templateUrl: "./crab-helper.html",
	styleUrl: "./crab-helper.scss",
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CrabHelper {}
