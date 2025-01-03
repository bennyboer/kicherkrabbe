import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-product.ts-page',
  templateUrl: './product.page.html',
  styleUrls: ['./product.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class ProductPage {}
