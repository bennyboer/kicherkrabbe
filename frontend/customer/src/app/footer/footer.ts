import { ChangeDetectionStrategy, Component } from "@angular/core";
import { RouterLink } from "@angular/router";
import { DarkModeToggle } from "../components";

@Component({
	selector: "app-footer",
	templateUrl: "./footer.html",
	styleUrl: "./footer.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
	imports: [RouterLink, DarkModeToggle],
})
export class Footer {}
