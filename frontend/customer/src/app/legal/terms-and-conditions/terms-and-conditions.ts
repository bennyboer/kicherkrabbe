import { ChangeDetectionStrategy, Component } from "@angular/core";

@Component({
	selector: "app-terms-and-conditions",
	templateUrl: "./terms-and-conditions.html",
	styleUrl: "../legal.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
})
export class TermsAndConditionsPage {}
