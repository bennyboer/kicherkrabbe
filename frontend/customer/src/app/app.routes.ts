import type { Routes } from "@angular/router";
import { FabricsPage } from "./fabrics/fabrics-page/fabrics-page";
import { HomePage } from "./home/home-page";
import { PatternsPage } from "./patterns/patterns-page/patterns-page";
import { ImprintPage } from "./legal/imprint/imprint";
import { TermsAndConditionsPage } from "./legal/terms-and-conditions/terms-and-conditions";
import { PrivacyPolicyPage } from "./legal/privacy-policy/privacy-policy";
import { CancellationPolicyPage } from "./legal/cancellation-policy/cancellation-policy";

export const routes: Routes = [
	{ path: "", component: HomePage },
	{ path: "patterns", component: PatternsPage },
	{ path: "fabrics", component: FabricsPage },
	{ path: "legal/imprint", component: ImprintPage },
	{ path: "legal/terms-and-conditions", component: TermsAndConditionsPage },
	{ path: "legal/privacy-policy", component: PrivacyPolicyPage },
	{ path: "legal/cancellation-policy", component: CancellationPolicyPage },
];
