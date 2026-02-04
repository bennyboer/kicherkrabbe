import { RenderMode, type ServerRoute } from "@angular/ssr";

export const serverRoutes: ServerRoute[] = [
	{
		path: "patterns/:id",
		renderMode: RenderMode.Server,
	},
	{
		path: "fabrics/:id",
		renderMode: RenderMode.Server,
	},
	{
		path: "**",
		renderMode: RenderMode.Prerender,
	},
];
