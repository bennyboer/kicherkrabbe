import { isPlatformBrowser } from "@angular/common";
import { Injectable, PLATFORM_ID, inject } from "@angular/core";
import { Option, none, some } from "../util/option";

@Injectable({ providedIn: "root" })
export class SeedService {
	private readonly platformId = inject(PLATFORM_ID);
	private readonly isBrowser = isPlatformBrowser(this.platformId);
	private seed: Option<number> = none();

	getSeed(): Option<number> {
		if (!this.isBrowser) {
			return none();
		}

		if (this.seed.isSome()) {
			return this.seed;
		}

		const stored = sessionStorage.getItem("featured_seed");
		if (stored) {
			this.seed = some(parseInt(stored, 10));
		} else {
			const newSeed = Math.floor(Math.random() * 2147483647);
			sessionStorage.setItem("featured_seed", newSeed.toString());
			this.seed = some(newSeed);
		}

		return this.seed;
	}
}
