import { ChangeDetectionStrategy, Component } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { Toast } from "primeng/toast";
import { BackgroundComponent } from "./background/background";
import { Footer } from "./footer/footer";
import { Header } from "./header/header";

@Component({
	selector: "app-root",
	imports: [RouterOutlet, Header, BackgroundComponent, Footer, Toast],
	templateUrl: "./app.html",
	styleUrl: "./app.scss",
	standalone: true,
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {}
