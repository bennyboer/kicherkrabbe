import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ContentChild,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  QueryList,
  TemplateRef,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { BehaviorSubject, map, Observable, Subject, takeUntil } from 'rxjs';
import { none, Option, Point, some, someOrNone } from '../../../../util';
import {
  NotificationService,
  OverlayRef,
  OverlayService,
} from '../../services';
import { Size as ButtonSize } from '../button/button.component';

export class Chip {
  readonly id: string;
  readonly label: string;
  readonly content: any;

  private constructor(props: { id: string; label: string; content?: any }) {
    this.id = someOrNone(props.id).orElseThrow('Chip ID is required');
    this.label = someOrNone(props.label).orElseThrow('Chip label is required');
    this.content = someOrNone(props.content).orElse(null);
  }

  static of(props: { id: string; label: string; content?: any }): Chip {
    return new Chip({
      id: props.id,
      label: props.label,
      content: props.content,
    });
  }
}

@Component({
  selector: 'app-chips',
  templateUrl: './chips.component.html',
  styleUrls: ['./chips.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChipsComponent implements OnDestroy, AfterViewInit, OnInit {
  @ContentChild(TemplateRef)
  chipTemplateRef!: TemplateRef<any>;

  @ViewChild('searchOverlayTemplate')
  searchOverlayTemplate!: TemplateRef<any>;

  @ViewChildren('addChipTextField')
  addChipTextFieldElements!: QueryList<ElementRef>;

  @Input()
  set chips(chips: Chip[]) {
    someOrNone(chips).ifSome((chips) => this.chips$.next(chips));
  }

  @Input()
  set available(chips: Chip[]) {
    someOrNone(chips).ifSome((chips) => this.availableChips$.next(chips));
  }

  @Input()
  type: string = 'Chip';

  @Output()
  added: EventEmitter<Chip> = new EventEmitter<Chip>();

  @Output()
  removed: EventEmitter<Chip> = new EventEmitter<Chip>();

  private readonly chips$: BehaviorSubject<Chip[]> = new BehaviorSubject<
    Chip[]
  >([]);
  private readonly availableChips$: BehaviorSubject<Chip[]> =
    new BehaviorSubject<Chip[]>([]);
  private readonly adding$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly search$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');
  private readonly searchResults$: BehaviorSubject<Chip[]> =
    new BehaviorSubject<Chip[]>([]);
  private readonly destroy$: Subject<void> = new Subject<void>();

  private overlay: Option<OverlayRef> = none();

  protected readonly ButtonSize = ButtonSize;

  constructor(
    private readonly overlayService: OverlayService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnInit(): void {
    this.adding$.pipe(takeUntil(this.destroy$)).subscribe((adding) => {
      if (!adding) {
        this.closeOverlayIfOpen();
      }
    });

    this.search$.pipe(takeUntil(this.destroy$)).subscribe((search) => {
      const isEmpty = search.length === 0;
      if (isEmpty) {
        this.searchResults$.next([]);
      } else {
        const availableChips = this.availableChips$.value;
        const result = availableChips.filter((chip) =>
          chip.label.toLowerCase().includes(search),
        );
        this.searchResults$.next(result);
      }
    });

    this.searchResults$.pipe(takeUntil(this.destroy$)).subscribe((result) => {
      const isEmpty = result.length === 0;
      if (isEmpty) {
        this.closeOverlayIfOpen();
      } else {
        this.makeSureOverlayIsOpen();
      }
    });
  }

  ngAfterViewInit(): void {
    this.addChipTextFieldElements.changes
      .pipe(takeUntil(this.destroy$))
      .subscribe((elements) => {
        if (elements.length === 0) {
          return;
        }

        this.onAddChipTextFieldAppeared(elements.first.nativeElement);
      });
  }

  ngOnDestroy(): void {
    this.chips$.complete();
    this.availableChips$.complete();
    this.adding$.complete();
    this.search$.complete();

    this.destroy$.next();
    this.destroy$.complete();

    this.closeOverlayIfOpen();
  }

  getChips(): Observable<Chip[]> {
    return this.chips$.asObservable();
  }

  isAdding(): Observable<boolean> {
    return this.adding$.asObservable();
  }

  showAddButton(): Observable<boolean> {
    return this.isAdding().pipe(map((adding) => !adding));
  }

  onAddChipTextFieldAppeared(input: HTMLInputElement): void {
    input.focus();
  }

  onAddChipTextFieldValueChanged(value: string): void {
    this.search$.next(value.trim().toLowerCase());
  }

  startAddingChip(): void {
    this.adding$.next(true);
  }

  addChipIfAvailable(label: string): void {
    let chip = someOrNone(
      this.availableChips$.value.find((chip) => chip.label === label),
    );
    if (chip.isNone()) {
      if (this.searchResults$.value.length === 1) {
        chip = some(this.searchResults$.value[0]);
      }
    }

    chip.ifSomeOrElse(
      (c) => this.addChip(c),
      () =>
        this.notificationService.publish({
          message: `${this.type} mit dem Namen "${label}" kann nicht hinzugefügt werden. Wähle eine Option aus der Liste.`,
          type: 'error',
        }),
    );
  }

  isChipAvailable(label: string): boolean {
    return this.availableChips$.value.some((chip) => chip.label === label);
  }

  cancelAddingChips(): void {
    this.adding$.next(false);
  }

  getSearchResults(): Observable<Chip[]> {
    return this.searchResults$.asObservable();
  }

  onAvailableChipSelected(chip: Chip): void {
    this.addChip(chip);
  }

  removeChip(chip: Chip): void {
    this.removed.emit(chip);
  }

  private addChip(chip: Chip): void {
    this.adding$.next(false);
    this.added.emit(chip);
  }

  private makeSureOverlayIsOpen(): void {
    this.overlay.ifNone(() => {
      const textField = someOrNone(this.addChipTextFieldElements.first)
        .map((input) => input.nativeElement)
        .orElseThrow('Add chip text field is not available');

      const overlay = this.overlayService.pushOverlay({
        templateRef: this.searchOverlayTemplate,
        parent: textField,
        offset: Point.of({
          x: 0,
          y: textField.offsetHeight,
        }),
        closeOnBackdropClick: true,
      });

      this.overlay = some(overlay);
    });
  }

  private closeOverlayIfOpen(): void {
    this.overlay.ifSome((overlay) => overlay.close());
    this.overlay = none();
  }
}
