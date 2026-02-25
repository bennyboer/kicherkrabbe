import type { Routes } from "@angular/router";
import { PatternsFilterState } from "./patterns/patterns-filter-state.service";
import { PatternsShell } from "./patterns/patterns-shell";
import { FabricsFilterState } from "./fabrics/fabrics-filter-state.service";
import { FabricsShell } from "./fabrics/fabrics-shell";
import { OffersFilterState } from "./offers/offers-filter-state.service";
import { OffersShell } from "./offers/offers-shell";

export const routes: Routes = [
	{
		path: "",
		loadComponent: () =>
			import("./home/home-page").then((m) => m.HomePage),
	},
	{
		path: "patterns",
		component: PatternsShell,
		providers: [PatternsFilterState],
		children: [
			{
				path: "",
				loadComponent: () =>
					import("./patterns/patterns-page/patterns-page").then(
						(m) => m.PatternsPage
					),
			},
			{
				path: ":id",
				loadComponent: () =>
					import("./patterns/pattern-detail-page/pattern-detail-page").then(
						(m) => m.PatternDetailPage
					),
			},
		],
	},
	{
		path: "fabrics",
		component: FabricsShell,
		providers: [FabricsFilterState],
		children: [
			{
				path: "",
				loadComponent: () =>
					import("./fabrics/fabrics-page/fabrics-page").then(
						(m) => m.FabricsPage
					),
			},
			{
				path: ":id",
				loadComponent: () =>
					import("./fabrics/fabric-detail-page/fabric-detail-page").then(
						(m) => m.FabricDetailPage
					),
			},
		],
	},
	{
		path: "offers",
		component: OffersShell,
		providers: [OffersFilterState],
		children: [
			{
				path: "",
				loadComponent: () =>
					import("./offers/offers-page/offers-page").then(
						(m) => m.OffersPage,
					),
			},
			{
				path: ":id",
				loadComponent: () =>
					import("./offers/offer-detail-page/offer-detail-page").then(
						(m) => m.OfferDetailPage,
					),
			},
		],
	},
	{
		path: "contact",
		loadComponent: () =>
			import("./contact").then((m) => m.ContactPage),
	},
	{
		path: "contact/sent",
		loadComponent: () =>
			import("./contact").then((m) => m.ContactSentPage),
	},
	{
		path: "landing/hochzeit",
		loadComponent: () =>
			import("./landing/wedding/wedding-page").then((m) => m.WeddingPage),
	},
	{
		path: "landing/besondere-anlaesse",
		loadComponent: () =>
			import("./landing/wedding/wedding-page").then((m) => m.WeddingPage),
	},
	{
		path: "landing/tracht",
		loadComponent: () =>
			import("./landing/tradition/tradition-page").then(
				(m) => m.TraditionPage
			),
	},
	{
		path: "legal/imprint",
		loadComponent: () =>
			import("./legal/imprint/imprint").then((m) => m.ImprintPage),
	},
	{
		path: "legal/terms-and-conditions",
		loadComponent: () =>
			import("./legal/terms-and-conditions/terms-and-conditions").then(
				(m) => m.TermsAndConditionsPage
			),
	},
	{
		path: "legal/privacy-policy",
		loadComponent: () =>
			import("./legal/privacy-policy/privacy-policy").then(
				(m) => m.PrivacyPolicyPage
			),
	},
	{
		path: "legal/cancellation-policy",
		loadComponent: () =>
			import("./legal/cancellation-policy/cancellation-policy").then(
				(m) => m.CancellationPolicyPage
			),
	},
	{
		path: "**",
		loadComponent: () =>
			import("./not-found/not-found-page").then((m) => m.NotFoundPage),
	},
];
