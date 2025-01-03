import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-products-page',
  templateUrl: './products.page.html',
  styleUrls: ['./products.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class ProductsPage {}
