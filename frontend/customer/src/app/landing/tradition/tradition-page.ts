import { ChangeDetectionStrategy, Component } from "@angular/core";
import { RouterLink } from "@angular/router";
import { Button } from "primeng/button";

@Component({
	selector: "app-tradition-page",
	templateUrl: "./tradition-page.html",
	styleUrl: "./tradition-page.scss",
	standalone: true,
	imports: [RouterLink, Button],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TraditionPage {}
