import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import { MenuItem } from "primeng/api";
import { Breadcrumb } from "primeng/breadcrumb";

export interface BreadcrumbItem {
	label: string;
	url?: string;
}

@Component({
	selector: "app-breadcrumbs",
	standalone: true,
	imports: [Breadcrumb],
	templateUrl: "./breadcrumbs.html",
	styleUrl: "./breadcrumbs.scss",
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Breadcrumbs {
	@Input() set items(value: BreadcrumbItem[]) {
		this.menuItems = value.map((item) => ({
			label: item.label,
			routerLink: item.url,
		}));
	}

	home: MenuItem = { icon: "pi pi-home", routerLink: "/" };
	menuItems: MenuItem[] = [];
}
