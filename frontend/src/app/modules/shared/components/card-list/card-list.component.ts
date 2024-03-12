import {
  ChangeDetectionStrategy,
  Component,
  Input,
  TemplateRef,
} from '@angular/core';
import { Option } from '../../../../util';

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
    this.title = Option.someOrNone(props.title).orElseThrow(
      'Title is required',
    );
    this.description = props.description;
    this.link = Option.someOrNone(props.link).orElseThrow('Link is required');
    this.imageUrl = Option.someOrNone(props.imageUrl).orElseThrow(
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
      description: Option.someOrNone(props.description),
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
    this.items = Option.someOrNone(items).orElse([]);
  }

  @Input('template')
  set setTemplate(template: TemplateRef<any>) {
    this.template = Option.someOrNone(template);
  }

  protected template: Option<TemplateRef<any>> = Option.none();

  protected items: CardListItem[] = [];
}
