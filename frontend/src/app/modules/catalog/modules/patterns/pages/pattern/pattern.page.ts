import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-pattern-page',
  templateUrl: './pattern.page.html',
  styleUrls: ['./pattern.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternPage {}
