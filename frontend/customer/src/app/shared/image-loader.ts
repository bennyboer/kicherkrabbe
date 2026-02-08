import { IMAGE_CONFIG, ImageConfig, ImageLoaderConfig } from "@angular/common";
import { Provider } from "@angular/core";
import { environment } from "../../environments";

export function assetImageLoader(config: ImageLoaderConfig): string {
	const baseUrl = `${environment.apiUrl}/assets/${config.src}/content`;

	if (config.width) {
		return `${baseUrl}?width=${config.width}`;
	}

	return baseUrl;
}

export const ASSET_IMAGE_CONFIG: Provider = {
	provide: IMAGE_CONFIG,
	useValue: {
		breakpoints: [384, 768, 1536, 3072],
	} satisfies ImageConfig,
};
