import { ChangeDetectionStrategy, Component } from "@angular/core";
import { RouterLink } from "@angular/router";
import { Button } from "primeng/button";

@Component({
	selector: "app-wedding-page",
	templateUrl: "./wedding-page.html",
	styleUrl: "./wedding-page.scss",
	standalone: true,
	imports: [RouterLink, Button],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WeddingPage {}
