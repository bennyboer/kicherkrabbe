import {
  ChangeDetectionStrategy,
  Component,
  Input,
  TemplateRef,
} from '@angular/core';
import { none, Option, someOrNone } from '../../../../util';

export class CardListItem {
  readonly title: string;
  readonly description: Option<string>;
  readonly link: string;
  readonly imageUrl: string;

  private constructor(props: {
    title: string;
    description: Option<string>;
    link: string;
    imageUrl: string;
  }) {
    this.title = someOrNone(props.title).orElseThrow('Title is required');
    this.description = props.description;
    this.link = someOrNone(props.link).orElseThrow('Link is required');
    this.imageUrl = someOrNone(props.imageUrl).orElseThrow(
      'Image URL is required',
    );
  }

  static of(props: {
    title: string;
    description?: string;
    link: string;
    imageUrl: string;
  }): CardListItem {
    return new CardListItem({
      title: props.title,
      description: someOrNone(props.description),
      link: props.link,
      imageUrl: props.imageUrl,
    });
  }
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
    this.items = someOrNone(items).orElse([]);
  }

  @Input('template')
  set setTemplate(template: TemplateRef<any>) {
    this.template = someOrNone(template);
  }

  protected template: Option<TemplateRef<any>> = none();

  protected items: CardListItem[] = [];
}
