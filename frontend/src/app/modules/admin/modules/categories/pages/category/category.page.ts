import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-category-page',
  templateUrl: './category.page.html',
  styleUrls: ['./category.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoryPage {}
