import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-fabric-details-page',
  templateUrl: './details.page.html',
  styleUrls: ['./details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FabricDetailsPage {}
