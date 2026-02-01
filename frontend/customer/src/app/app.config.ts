import {
	type ApplicationConfig,
	provideBrowserGlobalErrorListeners,
	provideZonelessChangeDetection,
} from "@angular/core";
import { provideHttpClient, withFetch } from "@angular/common/http";
import {
	provideClientHydration,
	withEventReplay,
} from "@angular/platform-browser";
import { provideAnimationsAsync } from "@angular/platform-browser/animations/async";
import { provideRouter } from "@angular/router";
import { providePrimeNG } from "primeng/config";
import { routes } from "./app.routes";
import { customerPreset } from "./preset";

export const appConfig: ApplicationConfig = {
	providers: [
		provideBrowserGlobalErrorListeners(),
		provideZonelessChangeDetection(),
		provideRouter(routes),
		provideHttpClient(withFetch()),
		provideAnimationsAsync(),
		providePrimeNG({
			theme: {
				preset: customerPreset,
				options: {
					darkModeSelector: ".kicherkrabbe-dark-mode",
				},
			},
		}),
		provideClientHydration(withEventReplay()),
	],
};
