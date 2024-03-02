import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CardListItem } from '../../../shared';

@Component({
  selector: 'app-catalog-page',
  templateUrl: './catalog.page.html',
  styleUrls: ['./catalog.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogPage {
  readonly items: CardListItem[] = [
    {
      title: 'Schnitte',
      link: 'patterns',
      imageUrl: '/assets/examples/example.jpg',
    },
    {
      title: 'Stoffe',
      link: 'fabrics',
      imageUrl: '/assets/examples/example.jpg',
    },
    {
      title: 'Stickereien',
      link: 'embroideries',
      imageUrl: '/assets/examples/example.jpg',
    },
    {
      title: 'Accessoires',
      link: 'accessories',
      imageUrl: '/assets/examples/example.jpg',
    },
  ];
}
