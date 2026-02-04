import { Component } from "@angular/core";
import { RouterOutlet } from "@angular/router";

@Component({
	selector: "app-patterns-shell",
	template: "<router-outlet />",
	standalone: true,
	imports: [RouterOutlet],
})
export class PatternsShell {}
