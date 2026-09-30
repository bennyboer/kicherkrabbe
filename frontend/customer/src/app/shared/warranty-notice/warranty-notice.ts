import { ChangeDetectionStrategy, Component, signal } from "@angular/core";
import { RouterLink } from "@angular/router";
import { Dialog } from "primeng/dialog";

@Component({
	selector: "app-warranty-notice",
	templateUrl: "./warranty-notice.html",
	styleUrl: "./warranty-notice.scss",
	standalone: true,
	imports: [RouterLink, Dialog],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WarrantyNotice {
	readonly visible = signal(false);
}
