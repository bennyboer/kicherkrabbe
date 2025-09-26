import { Component } from "@angular/core";
import { ButtonModule } from "primeng/button";
import { BackgroundComponent } from "./background/background";
import { CrabHelper } from "./crab-helper/crab-helper";
import { Header } from "./header/header";
import { HighlightsComponent } from "./highlights/highlights";

@Component({
	selector: "app-root",
	imports: [
		ButtonModule,
		Header,
		HighlightsComponent,
		BackgroundComponent,
		CrabHelper,
	],
	templateUrl: "./app.html",
	styleUrl: "./app.scss",
	standalone: true,
})
export class App {
	toggleDarkMode(): void {
		const element = document.querySelector("html");
		element?.classList.toggle("kicherkrabbe-dark-mode");
	}
}
