import {
  ChangeDetectionStrategy,
  Component,
  ContentChild,
  ElementRef,
  HostListener,
  Input,
  OnDestroy,
  TemplateRef,
} from '@angular/core';
import { BehaviorSubject, Observable, ReplaySubject, Subject } from 'rxjs';
import { Option } from '../../../../util';

export interface DropdownItem {
  id: string;
}

@Component({
  selector: 'app-dropdown',
  templateUrl: './dropdown.component.html',
  styleUrls: ['./dropdown.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DropdownComponent<I extends DropdownItem> implements OnDestroy {
  @ContentChild(TemplateRef)
  itemTemplate!: TemplateRef<any>;

  @Input()
  placeholder: string = '';

  @Input('items')
  set setItems(items: I[]) {
    Option.someOrNone(items).ifSome((items) => this.items$.next(items));
  }

  private readonly items$: Subject<I[]> = new ReplaySubject<I[]>(1);
  private readonly opened$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);

  constructor(private readonly elementRef: ElementRef) {}

  ngOnDestroy(): void {
    this.items$.complete();
    this.opened$.complete();
  }

  @HostListener('document:click', ['$event'])
  onClickOut(event: MouseEvent) {
    const isClickOutsideComponent = !this.elementRef.nativeElement.contains(
      event.target,
    );
    const isCurrentlyOpened = this.opened$.value;

    if (isClickOutsideComponent && isCurrentlyOpened) {
      this.toggle();
    }
  }

  isOpened(): Observable<boolean> {
    return this.opened$.asObservable();
  }

  getItems(): Observable<I[]> {
    return this.items$.asObservable();
  }

  toggle(): void {
    this.opened$.next(!this.opened$.value);
  }
}
