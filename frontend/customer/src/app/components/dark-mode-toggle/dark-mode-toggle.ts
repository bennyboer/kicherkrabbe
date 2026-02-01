import {
	ChangeDetectionStrategy,
	Component,
	ElementRef,
	HostListener,
	inject,
} from "@angular/core";
import { AsyncPipe } from "@angular/common";
import { map, Observable, tap } from "rxjs";
import { Theme, ThemeService } from "../../services/theme.service";

@Component({
	selector: "app-dark-mode-toggle",
	templateUrl: "./dark-mode-toggle.html",
	styleUrl: "./dark-mode-toggle.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
	imports: [AsyncPipe],
})
export class DarkModeToggle {
	private readonly themeService = inject(ThemeService);
	private readonly elementRef = inject(ElementRef);

	readonly isDarkMode$: Observable<boolean> = this.themeService.getTheme().pipe(
		map((theme) => theme === Theme.DARK),
		tap((isDarkMode) => {
			const element: HTMLElement = this.elementRef.nativeElement;
			if (isDarkMode) {
				element.classList.add("dark-mode");
			} else {
				element.classList.remove("dark-mode");
			}
		})
	);

	@HostListener("click")
	toggleDarkMode(): void {
		this.themeService.toggleTheme();
	}
}
