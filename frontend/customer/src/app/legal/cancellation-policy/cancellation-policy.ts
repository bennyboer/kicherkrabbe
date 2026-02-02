import { ChangeDetectionStrategy, Component } from "@angular/core";

@Component({
	selector: "app-cancellation-policy",
	templateUrl: "./cancellation-policy.html",
	styleUrl: "../legal.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
})
export class CancellationPolicyPage {}
