import {ChangeDetectionStrategy, Component} from "@angular/core";

@Component({
  selector: "app-dashboard-page",
  templateUrl: "./dashboard.page.html",
  styleUrls: ["./dashboard.page.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardPage {

}
