import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { Option } from '../../../../util';

export interface CardListItem {
  title: string;
  description?: string;
  link: string;
  imageUrl: string;
}

@Component({
  selector: 'app-card-list',
  templateUrl: './card-list.component.html',
  styleUrls: ['./card-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardListComponent {
  @Input('items')
  set setItems(items: CardListItem[]) {
    this.items = Option.someOrNone(items).orElse([]);
  }

  protected items: CardListItem[] = [];
}
