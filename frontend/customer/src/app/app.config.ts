import { provideHttpClient, withFetch } from "@angular/common/http";
import {
	type ApplicationConfig,
	provideBrowserGlobalErrorListeners,
	provideZonelessChangeDetection,
} from "@angular/core";
import {
	provideClientHydration,
	withEventReplay,
} from "@angular/platform-browser";
import { provideAnimationsAsync } from "@angular/platform-browser/animations/async";
import { provideRouter, withInMemoryScrolling } from "@angular/router";
import { MessageService } from "primeng/api";
import { providePrimeNG } from "primeng/config";
import { routes } from "./app.routes";
import { customerPreset } from "./preset";

export const appConfig: ApplicationConfig = {
	providers: [
		provideBrowserGlobalErrorListeners(),
		provideZonelessChangeDetection(),
		provideHttpClient(withFetch()),
		provideRouter(
			routes,
			withInMemoryScrolling({
				scrollPositionRestoration: "enabled",
			}),
		),
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
		MessageService,
	],
};
