import { Component } from "@angular/core";
import { BackgroundComponent } from "./background/background";
import { Badges } from "./badges/badges";
import { DarkModeToggle } from "./components";
import { CrabHelper } from "./crab-helper/crab-helper";
import { FeaturedFabrics } from "./fabrics";
import { Header } from "./header/header";
import { HighlightsComponent } from "./highlights/highlights";
import { FeaturedPatterns } from "./patterns";

@Component({
	selector: "app-root",
	imports: [
		Header,
		HighlightsComponent,
		BackgroundComponent,
		CrabHelper,
		Badges,
		FeaturedPatterns,
		FeaturedFabrics,
		DarkModeToggle,
	],
	templateUrl: "./app.html",
	styleUrl: "./app.scss",
	standalone: true,
})
export class App {}
