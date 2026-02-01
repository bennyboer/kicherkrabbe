import { isPlatformBrowser } from "@angular/common";
import { Injectable, OnDestroy, PLATFORM_ID, inject } from "@angular/core";
import {
	BehaviorSubject,
	distinctUntilChanged,
	Observable,
	Subject,
	takeUntil,
} from "rxjs";

export enum Theme {
	LIGHT = "LIGHT",
	DARK = "DARK",
}

const THEME_PREFERENCE_KEY = "theme-preference";
const PREFERENCE_EXPIRY_MS = 60 * 60 * 1000; // 1 hour

interface StoredPreference {
	theme: Theme;
	timestamp: number;
}

@Injectable({
	providedIn: "root",
})
export class ThemeService implements OnDestroy {
	private readonly platformId = inject(PLATFORM_ID);
	private readonly isBrowser = isPlatformBrowser(this.platformId);

	private readonly theme$: BehaviorSubject<Theme> = new BehaviorSubject<Theme>(
		Theme.LIGHT
	);
	private readonly destroy$: Subject<void> = new Subject<void>();

	private darkModeMediaQuery!: MediaQueryList;
	private darkModeEventListener!: (event: MediaQueryListEvent) => void;

	constructor() {
		if (this.isBrowser) {
			this.theme$
				.pipe(takeUntil(this.destroy$))
				.subscribe((theme) => this.applyTheme(theme));

			this.initializeTheme();
		}
	}

	ngOnDestroy(): void {
		this.theme$.complete();

		this.destroy$.next();
		this.destroy$.complete();

		if (this.isBrowser) {
			this.deregisterSystemPreferencesListener();
		}
	}

	setTheme(theme: Theme, isUserAction: boolean = false): void {
		if (isUserAction && this.isBrowser) {
			this.storePreference(theme);
		}
		this.theme$.next(theme);
	}

	getTheme(): Observable<Theme> {
		return this.theme$.asObservable().pipe(distinctUntilChanged());
	}

	toggleTheme(): void {
		const currentTheme = this.theme$.value;
		const newTheme = currentTheme === Theme.DARK ? Theme.LIGHT : Theme.DARK;
		this.setTheme(newTheme, true);
	}

	private initializeTheme(): void {
		const storedPreference = this.getStoredPreference();

		if (storedPreference && !this.isPreferenceExpired(storedPreference)) {
			this.setTheme(storedPreference.theme);
		} else {
			this.clearStoredPreference();
			this.applySystemPreference();
		}

		this.listenToSystemPreferences();
	}

	private applyTheme(theme: Theme): void {
		const html = document.documentElement;

		if (theme === Theme.DARK) {
			html.classList.add("kicherkrabbe-dark-mode");
		} else {
			html.classList.remove("kicherkrabbe-dark-mode");
		}
	}

	private listenToSystemPreferences(): void {
		this.darkModeMediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
		this.darkModeEventListener = (event: MediaQueryListEvent) => {
			const storedPreference = this.getStoredPreference();
			if (!storedPreference || this.isPreferenceExpired(storedPreference)) {
				this.clearStoredPreference();
				if (event.matches) {
					this.setTheme(Theme.DARK);
				} else {
					this.setTheme(Theme.LIGHT);
				}
			}
		};
		this.darkModeMediaQuery.addEventListener(
			"change",
			this.darkModeEventListener
		);
	}

	private applySystemPreference(): void {
		const prefersDark = window.matchMedia(
			"(prefers-color-scheme: dark)"
		).matches;
		this.setTheme(prefersDark ? Theme.DARK : Theme.LIGHT);
	}

	private deregisterSystemPreferencesListener(): void {
		if (this.darkModeMediaQuery && this.darkModeEventListener) {
			this.darkModeMediaQuery.removeEventListener(
				"change",
				this.darkModeEventListener
			);
		}
	}

	private storePreference(theme: Theme): void {
		const preference: StoredPreference = {
			theme,
			timestamp: Date.now(),
		};
		localStorage.setItem(THEME_PREFERENCE_KEY, JSON.stringify(preference));
	}

	private getStoredPreference(): StoredPreference | null {
		const stored = localStorage.getItem(THEME_PREFERENCE_KEY);
		if (!stored) {
			return null;
		}
		try {
			return JSON.parse(stored) as StoredPreference;
		} catch {
			return null;
		}
	}

	private isPreferenceExpired(preference: StoredPreference): boolean {
		return Date.now() - preference.timestamp > PREFERENCE_EXPIRY_MS;
	}

	private clearStoredPreference(): void {
		localStorage.removeItem(THEME_PREFERENCE_KEY);
	}
}
