import { RenderMode, type ServerRoute } from "@angular/ssr";

export const serverRoutes: ServerRoute[] = [
	{
		path: "",
		renderMode: RenderMode.Server,
	},
	{
		path: "patterns",
		renderMode: RenderMode.Server,
	},
	{
		path: "patterns/:id",
		renderMode: RenderMode.Server,
	},
	{
		path: "fabrics",
		renderMode: RenderMode.Server,
	},
	{
		path: "fabrics/:id",
		renderMode: RenderMode.Server,
	},
	{
		path: "contact",
		renderMode: RenderMode.Server,
	},
	{
		path: "contact/sent",
		renderMode: RenderMode.Prerender,
	},
	{
		path: "landing/hochzeit",
		renderMode: RenderMode.Prerender,
	},
	{
		path: "landing/tracht",
		renderMode: RenderMode.Prerender,
	},
	{
		path: "legal/imprint",
		renderMode: RenderMode.Prerender,
	},
	{
		path: "legal/terms-and-conditions",
		renderMode: RenderMode.Prerender,
	},
	{
		path: "legal/privacy-policy",
		renderMode: RenderMode.Prerender,
	},
	{
		path: "legal/cancellation-policy",
		renderMode: RenderMode.Prerender,
	},
	{
		path: "**",
		renderMode: RenderMode.Server,
		status: 404,
	},
];
