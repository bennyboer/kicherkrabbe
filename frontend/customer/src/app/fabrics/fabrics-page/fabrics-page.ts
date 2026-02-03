import { ChangeDetectionStrategy, Component } from "@angular/core";

@Component({
	selector: "app-fabrics-page",
	templateUrl: "./fabrics-page.html",
	styleUrl: "./fabrics-page.scss",
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FabricsPage {}
