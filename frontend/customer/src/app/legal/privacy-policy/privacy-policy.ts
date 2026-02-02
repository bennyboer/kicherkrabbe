import { ChangeDetectionStrategy, Component } from "@angular/core";

@Component({
	selector: "app-privacy-policy",
	templateUrl: "./privacy-policy.html",
	styleUrl: "../legal.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
})
export class PrivacyPolicyPage {}
