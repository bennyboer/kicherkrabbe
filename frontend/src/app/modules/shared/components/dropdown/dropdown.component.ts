import {
  ChangeDetectionStrategy,
  Component,
  ContentChild,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import {
  BehaviorSubject,
  map,
  Observable,
  of,
  Subject,
  switchMap,
  takeUntil,
} from 'rxjs';
import {
  none,
  Option,
  Point,
  Rect,
  Size,
  some,
  someOrNone,
} from '../../../../util';
import { OverlayRef, OverlayService } from '../../services';
import { ButtonSize as ButtonSize } from '../button/button.component';

export type DropdownItemId = string;

export interface DropdownItem {
  id: DropdownItemId;
  label: string;
}

@Component({
  selector: 'app-dropdown',
  templateUrl: './dropdown.component.html',
  styleUrls: ['./dropdown.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DropdownComponent implements OnDestroy, OnInit {
  @ContentChild(TemplateRef)
  itemTemplate!: TemplateRef<any>;

  @ViewChild('dropdownTemplate')
  dropdownTemplateRef!: TemplateRef<any>;

  @Input()
  label: string = '';

  @Input()
  multiple: boolean = false;

  @Input()
  useRadioButtonForSingleSelection: boolean = false;

  @Input()
  showSelectionIndicator: boolean = false;

  @Input('items')
  set setItems(items: DropdownItem[]) {
    someOrNone(items).ifSome((items) => this.items$.next(items));
  }

  @Input('selected')
  set setSelected(selected: DropdownItemId[]) {
    someOrNone(selected).ifSome((selected) => {
      this.selected$.next(new Set(selected));
    });
  }

  @Output()
  selectionChanged: EventEmitter<DropdownItemId[]> = new EventEmitter<
    DropdownItemId[]
  >();

  protected readonly ButtonSize = ButtonSize;

  private readonly items$: BehaviorSubject<DropdownItem[]> =
    new BehaviorSubject<DropdownItem[]>([]);
  private readonly selected$: BehaviorSubject<Set<DropdownItemId>> =
    new BehaviorSubject<Set<DropdownItemId>>(new Set<DropdownItemId>());
  private readonly destroy$: Subject<void> = new Subject<void>();
  private readonly openedOverlay$: BehaviorSubject<Option<OverlayRef>> =
    new BehaviorSubject<Option<OverlayRef>>(none());

  constructor(
    private readonly elementRef: ElementRef,
    private readonly overlayService: OverlayService,
  ) {}

  ngOnInit(): void {
    this.selected$.pipe(takeUntil(this.destroy$)).subscribe((selected) => {
      this.selectionChanged.emit(Array.from(selected));
    });
  }

  ngOnDestroy(): void {
    this.closeOpenedOverlay();

    this.openedOverlay$.complete();
    this.items$.complete();
    this.selected$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getSelected(): Observable<DropdownItemId[]> {
    return this.selected$
      .asObservable()
      .pipe(map((selected) => Array.from(selected)));
  }

  isSelected(id: DropdownItemId): Observable<boolean> {
    return this.getSelected().pipe(map((selected) => selected.includes(id)));
  }

  toggleItemSelection(id: DropdownItemId): void {
    const updatedSet = new Set([...this.selected$.value]);
    const isSelected = updatedSet.has(id);

    if (isSelected) {
      if (this.multiple) {
        const updated = updatedSet.delete(id);
        if (updated) {
          this.selected$.next(updatedSet);
        }
      }
    } else {
      if (!this.multiple) {
        updatedSet.clear();
      }

      updatedSet.add(id);

      this.selected$.next(updatedSet);
    }
  }

  isOpened(): Observable<boolean> {
    return this.openedOverlay$.pipe(
      switchMap((overlay) =>
        overlay.map((o) => o.isOpened()).orElse(of(false)),
      ),
    );
  }

  getItems(): Observable<DropdownItem[]> {
    return this.items$.asObservable();
  }

  toggleOpened(): void {
    const openedOverlayId = this.openedOverlay$.value;
    const isOpened = openedOverlayId
      .map((overlay) => overlay.isCurrentlyOpened())
      .orElse(false);

    if (isOpened) {
      this.closeOpenedOverlay();
      this.openedOverlay$.next(none());
    } else {
      const rect = this.getElementRect();

      this.openedOverlay$.next(
        some(
          this.overlayService.pushOverlay({
            templateRef: this.dropdownTemplateRef,
            parent: this.elementRef.nativeElement as HTMLElement,
            offset: Point.of({ x: 0, y: rect.size.height }),
            minWidth: rect.size.width,
          }),
        ),
      );
    }
  }

  selectAll(): void {
    const itemIds = this.items$.value.map((item) => item.id);
    this.selected$.next(new Set(itemIds));
  }

  clearSelection(): void {
    this.selected$.next(new Set<DropdownItemId>());
  }

  onItemClick(item: DropdownItem): void {
    this.toggleItemSelection(item.id);
  }

  getSelectedLabel(selected: DropdownItemId[]): string {
    if (selected.length === 0) {
      return this.label;
    }

    const selectedItemId = selected[0];
    const selectedItem = this.items$.value.find(
      (item) => item.id === selectedItemId,
    );
    return selectedItem ? selectedItem.label : this.label;
  }

  private getElementRect(): Rect {
    const element = this.elementRef.nativeElement as HTMLElement;
    const rect = element.getBoundingClientRect();

    const position = Point.of({ x: rect.left, y: rect.top });
    const size = Size.of({ width: rect.width, height: rect.height });

    return Rect.of({
      position,
      size,
    });
  }

  private closeOpenedOverlay(): void {
    this.openedOverlay$.value.ifSome((overlay) => overlay.close());
  }
}
