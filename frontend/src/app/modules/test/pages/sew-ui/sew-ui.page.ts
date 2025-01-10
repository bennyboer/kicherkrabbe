import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-sew-ui-page',
  templateUrl: './sew-ui.page.html',
  styleUrls: ['./sew-ui.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class SewUiPage {}
