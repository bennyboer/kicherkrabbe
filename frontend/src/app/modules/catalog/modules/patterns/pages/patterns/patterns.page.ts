import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-patterns-page',
  templateUrl: './patterns.page.html',
  styleUrls: ['./patterns.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternsPage {
  protected readonly patterns = Array(20).fill(0);
}
