import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-categories-page',
  templateUrl: './categories.page.html',
  styleUrls: ['./categories.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoriesPage {}
