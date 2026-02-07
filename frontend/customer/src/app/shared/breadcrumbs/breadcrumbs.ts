import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import { RouterLink } from "@angular/router";

export interface BreadcrumbItem {
	label: string;
	url?: string;
}

@Component({
	selector: "app-breadcrumbs",
	standalone: true,
	imports: [RouterLink],
	templateUrl: "./breadcrumbs.html",
	styleUrl: "./breadcrumbs.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Breadcrumbs {
	@Input() items: BreadcrumbItem[] = [];
}
