import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { Option } from '../../../../util';

export class SortProperty {
  readonly id: string;
  readonly label: string;

  private constructor(props: { id: string; label: string }) {
    this.id = props.id;
    this.label = props.label;
  }

  static of(props: { id: string; label: string }): SortProperty {
    return new SortProperty({
      id: props.id,
      label: props.label,
    });
  }
}

export class SortedEvent {
  readonly property: SortProperty;
  readonly ascending: boolean;

  private constructor(props: { property: SortProperty; ascending: boolean }) {
    this.property = props.property;
    this.ascending = props.ascending;
  }

  static of(props: {
    property: SortProperty;
    ascending: boolean;
  }): SortedEvent {
    return new SortedEvent({
      property: props.property,
      ascending: props.ascending,
    });
  }
}

@Component({
  selector: 'app-sort-selector',
  templateUrl: './sort-selector.component.html',
  styleUrls: ['./sort-selector.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SortSelectorComponent implements OnInit {
  @ViewChild('select', { static: true })
  select!: ElementRef<HTMLSelectElement>;

  @Input()
  properties: SortProperty[] = [];

  @Output()
  sorted: EventEmitter<SortedEvent> = new EventEmitter<SortedEvent>();

  ascending: boolean = true;

  ngOnInit(): void {
    this.publishSortedEvent();
  }

  protected toggleSortDirection(): void {
    this.ascending = !this.ascending;
    this.publishSortedEvent();
  }

  protected publishSortedEvent(): void {
    const property = Option.someOrNone(
      this.properties.find(
        (property) => property.id === this.select.nativeElement.value,
      ),
    ).orElse(this.properties[0]);

    const event = SortedEvent.of({
      property,
      ascending: this.ascending,
    });

    this.sorted.emit(event);
  }

  protected getSortDirectionLabel(): string {
    return this.ascending ? 'Aufsteigend' : 'Absteigend';
  }
}
