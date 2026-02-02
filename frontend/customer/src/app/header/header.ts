import { Component } from "@angular/core";
import { RouterLink } from "@angular/router";

@Component({
	selector: "app-header",
	templateUrl: "./header.html",
	styleUrl: "./header.scss",
	standalone: true,
	imports: [RouterLink],
})
export class Header {}
