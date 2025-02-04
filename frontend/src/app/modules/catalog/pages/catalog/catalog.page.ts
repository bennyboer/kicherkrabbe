import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CardListItem } from '../../../shared';

@Component({
    selector: 'app-catalog-page',
    templateUrl: './catalog.page.html',
    styleUrls: ['./catalog.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class CatalogPage {
  readonly items: CardListItem[] = [
    CardListItem.of({
      title: 'Schnitte',
      link: 'patterns',
      imageUrl: '/assets/examples/example.jpg',
    }),
    CardListItem.of({
      title: 'Stoffe',
      link: 'fabrics',
      imageUrl: '/assets/examples/example.jpg',
    }),
  ];
}
