import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CardListItem } from '../../../../../shared';

@Component({
  selector: 'app-patterns-page',
  templateUrl: './patterns.page.html',
  styleUrls: ['./patterns.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternsPage {
  protected readonly patterns: CardListItem[] = Array(20)
    .fill(0)
    .map((_, i) => ({
      title: 'Basic Sweater der Fünfte',
      description: 'ab 25,00 €',
      link: '/catalog/patterns/0', // TODO: Add correct link
      imageUrl: `/assets/examples/example.jpg`,
    }));
}
