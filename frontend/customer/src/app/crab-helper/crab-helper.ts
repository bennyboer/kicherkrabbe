import { ChangeDetectionStrategy, Component, output } from "@angular/core";

@Component({
	selector: "app-crab-helper",
	templateUrl: "./crab-helper.html",
	styleUrl: "./crab-helper.scss",
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CrabHelper {
	clicked = output<void>();
}
