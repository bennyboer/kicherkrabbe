import { Component } from "@angular/core";
import { ButtonModule } from "primeng/button";
import {Header} from "./header/header";
import {HighlightsComponent} from "./highlights/highlights";

@Component({
	selector: "app-root",
	imports: [ButtonModule, Header, HighlightsComponent],
	templateUrl: "./app.html",
	styleUrl: "./app.scss",
})
export class App {
	toggleDarkMode(): void {
		const element = document.querySelector("html");
		element?.classList.toggle("kicherkrabbe-dark-mode");
	}
}
