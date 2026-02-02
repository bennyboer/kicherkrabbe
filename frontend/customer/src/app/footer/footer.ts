import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { RouterLink } from "@angular/router";
import { AsyncPipe } from "@angular/common";
import { Observable, map } from "rxjs";
import { Theme, ThemeService } from "../services/theme.service";

@Component({
	selector: "app-footer",
	templateUrl: "./footer.html",
	styleUrl: "./footer.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
	imports: [RouterLink, AsyncPipe],
})
export class Footer {
	private readonly themeService = inject(ThemeService);

	readonly isDarkMode$: Observable<boolean> = this.themeService
		.getTheme()
		.pipe(map((theme) => theme === Theme.DARK));

	toggleTheme(): void {
		this.themeService.toggleTheme();
	}
}
