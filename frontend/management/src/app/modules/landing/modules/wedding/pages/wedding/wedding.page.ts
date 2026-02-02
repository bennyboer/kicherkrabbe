import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-wedding-page',
  templateUrl: './wedding.page.html',
  styleUrls: ['./wedding.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class WeddingPage {}
