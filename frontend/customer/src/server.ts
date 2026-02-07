import { join } from "node:path";
import {
	AngularNodeAppEngine,
	createNodeRequestHandler,
	isMainModule,
	writeResponseToNodeResponse,
} from "@angular/ssr/node";
import express from "express";

const browserDistFolder = join(import.meta.dirname, "../browser");
const API_URL = process.env["API_URL"] || "https://api.kicherkrabbe.com";
const SITE_URL = "https://kicherkrabbe.com";

const app = express();
const angularApp = new AngularNodeAppEngine();

interface PublishedPatternDTO {
	id: string;
	alias: string;
}

interface PublishedFabricDTO {
	id: string;
	alias: string;
}

interface SitemapCache {
	xml: string;
	timestamp: number;
}

let sitemapCache: SitemapCache | null = null;
const CACHE_TTL = 60 * 60 * 1000;

async function fetchPatterns(): Promise<PublishedPatternDTO[]> {
	try {
		const response = await fetch(`${API_URL}/patterns/published`, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({
				searchTerm: "",
				categories: [],
				sizes: [],
				sort: { property: "ALPHABETICAL", direction: "ASCENDING" },
				skip: 0,
				limit: 1000,
			}),
		});
		if (!response.ok) return [];
		const data = await response.json();
		return data.patterns || [];
	} catch {
		return [];
	}
}

async function fetchFabrics(): Promise<PublishedFabricDTO[]> {
	try {
		const response = await fetch(`${API_URL}/fabrics/published`, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({
				searchTerm: "",
				colorIds: [],
				topicIds: [],
				availability: { active: false, inStock: true },
				sort: { property: "ALPHABETICAL", direction: "ASCENDING" },
				skip: 0,
				limit: 1000,
			}),
		});
		if (!response.ok) return [];
		const data = await response.json();
		return data.fabrics || [];
	} catch {
		return [];
	}
}

function generateSitemapXml(
	patterns: PublishedPatternDTO[],
	fabrics: PublishedFabricDTO[]
): string {
	const staticRoutes = [
		"",
		"/patterns",
		"/fabrics",
		"/contact",
		"/landing/hochzeit",
		"/landing/tracht",
		"/legal/imprint",
		"/legal/terms-and-conditions",
		"/legal/privacy-policy",
		"/legal/cancellation-policy",
	];

	const urls: string[] = [];

	for (const route of staticRoutes) {
		urls.push(`
  <url>
    <loc>${SITE_URL}${route}</loc>
    <changefreq>weekly</changefreq>
    <priority>${route === "" ? "1.0" : "0.8"}</priority>
  </url>`);
	}

	for (const pattern of patterns) {
		urls.push(`
  <url>
    <loc>${SITE_URL}/patterns/${pattern.alias}</loc>
    <changefreq>weekly</changefreq>
    <priority>0.7</priority>
  </url>`);
	}

	for (const fabric of fabrics) {
		urls.push(`
  <url>
    <loc>${SITE_URL}/fabrics/${fabric.alias}</loc>
    <changefreq>weekly</changefreq>
    <priority>0.7</priority>
  </url>`);
	}

	return `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">${urls.join("")}
</urlset>`;
}

app.get("/sitemap.xml", async (_req, res) => {
	const now = Date.now();

	if (sitemapCache && now - sitemapCache.timestamp < CACHE_TTL) {
		res.set("Content-Type", "application/xml");
		res.send(sitemapCache.xml);
		return;
	}

	const [patterns, fabrics] = await Promise.all([
		fetchPatterns(),
		fetchFabrics(),
	]);

	const xml = generateSitemapXml(patterns, fabrics);
	sitemapCache = { xml, timestamp: now };

	res.set("Content-Type", "application/xml");
	res.send(xml);
});

app.use(
	express.static(browserDistFolder, {
		maxAge: "1y",
		index: false,
		redirect: false,
	}),
);

/**
 * Handle all other requests by rendering the Angular application.
 */
app.use((req, res, next) => {
	angularApp
		.handle(req)
		.then((response) => {
			if (response) {
				res.set("Cache-Control", "no-cache");
				return writeResponseToNodeResponse(response, res);
			}
			return next();
		})
		.catch(next);
});

export function startServer() {
	const port = process.env["PORT"] || 4000;
	app.listen(port, (error) => {
		if (error) {
			throw error;
		}

		console.log(`Node Express server listening on http://localhost:${port}`);
	});
}

const metaUrl = import.meta.url;
const isMain = isMainModule(metaUrl);
const isPM2 = process.env["PM2"] === "true";

if (isMain || isPM2) {
	startServer();
}

/**
 * Request handler used by the Angular CLI (for dev-server and during build) or Firebase Cloud Functions.
 */
export const reqHandler = createNodeRequestHandler(app);
