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
import { BehaviorSubject, combineLatest, map, Observable, Subject, takeUntil } from 'rxjs';
import { Point } from '../../../../util';
import { NotificationService, OverlayRef, OverlayService } from '../../services';
import { ButtonSize as ButtonSize } from '../button/button.component';
import { none, Option, some, someOrNone } from '../../modules/option';

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
    standalone: false
})
export class ChipsComponent implements OnDestroy, AfterViewInit, OnInit {
  @ContentChild(TemplateRef)
  chipTemplateRef!: TemplateRef<any>;

  @ViewChild('searchOverlayTemplate')
  searchOverlayTemplate!: TemplateRef<any>;

  @ViewChildren('addChipTextField')
  addChipTextFieldElements!: QueryList<ElementRef>;

  @Input({ required: true })
  chipDropdownItemTemplateRef!: TemplateRef<any>;

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

  /**
   * Whether the available options are always shown or only when the user starts typing.
   */
  @Input()
  alwaysShowOptions: boolean = false;

  @Output()
  added: EventEmitter<Chip> = new EventEmitter<Chip>();

  @Output()
  removed: EventEmitter<Chip> = new EventEmitter<Chip>();

  private readonly chips$: BehaviorSubject<Chip[]> = new BehaviorSubject<Chip[]>([]);
  private readonly availableChips$: BehaviorSubject<Chip[]> = new BehaviorSubject<Chip[]>([]);
  private readonly adding$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly search$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  private readonly searchResults$: BehaviorSubject<Chip[]> = new BehaviorSubject<Chip[]>([]);
  private readonly inputFocused$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly showOverlay$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
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

    const filteredAvailableChips$ = combineLatest([this.chips$, this.availableChips$]).pipe(
      map(([chips, availableChips]) => {
        const chipIds = new Set<string>(chips.map((chip) => chip.id));
        return availableChips.filter((c) => !chipIds.has(c.id));
      }),
    );

    combineLatest([this.search$, filteredAvailableChips$])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([search, filteredAvailableChips]) => {
        const isEmpty = search.length === 0;
        if (isEmpty) {
          if (this.alwaysShowOptions) {
            this.searchResults$.next(filteredAvailableChips);
          } else {
            this.searchResults$.next([]);
          }
        } else {
          const result = filteredAvailableChips.filter((chip) => chip.label.toLowerCase().includes(search));
          this.searchResults$.next(result);
        }
      });

    this.searchResults$.pipe(takeUntil(this.destroy$)).subscribe((result) => {
      const hasAtLeastOneResult = result.length > 0;
      this.showOverlay$.next(hasAtLeastOneResult || this.alwaysShowOptions);
    });

    combineLatest([this.showOverlay$, this.inputFocused$])
      .pipe(
        map(([showOverlay, inputFocused]) => showOverlay && inputFocused),
        takeUntil(this.destroy$),
      )
      .subscribe((show) => {
        if (show) {
          this.makeSureOverlayIsOpen();
        } else {
          this.closeOverlayIfOpen();
        }
      });
  }

  ngAfterViewInit(): void {
    this.addChipTextFieldElements.changes.pipe(takeUntil(this.destroy$)).subscribe((elements) => {
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
    this.inputFocused$.complete();
    this.showOverlay$.complete();

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
    /*
    For some reason, the input elements position is not yet calculated correctly when the component is initialized.
    Thus we wait a bit before we focus the input element.
     */
    setTimeout(() => input.focus(), 100);
  }

  onAddChipTextFieldBlurred(): void {
    this.inputFocused$.next(false);
  }

  onAddChipTextFieldFocused(): void {
    this.inputFocused$.next(true);
  }

  onAddChipTextFieldValueChanged(value: string): void {
    this.search$.next(value.trim().toLowerCase());
  }

  startAddingChip(): void {
    this.adding$.next(true);
  }

  addChipIfAvailable(label: string): void {
    let chip = someOrNone(this.availableChips$.value.find((chip) => chip.label === label));
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

  cancelAddingChips(): void {
    this.adding$.next(false);
    this.inputFocused$.next(false);
  }

  getSearchResults(): Observable<Chip[]> {
    return this.searchResults$.asObservable();
  }

  onAvailableChipSelected(chip: Chip): void {
    this.inputFocused$.next(false);
    this.addChip(chip);
  }

  onAvailableChipMouseDown(event: MouseEvent): void {
    event.preventDefault();
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
      const textField = someOrNone(this.addChipTextFieldElements)
        .map((inputs) => inputs.first)
        .map((input) => input.nativeElement)
        .orElseThrow('Text field not found');

      const overlay = this.overlayService.pushOverlay({
        templateRef: this.searchOverlayTemplate,
        parent: textField,
        offset: Point.of({
          x: 0,
          y: textField.offsetHeight,
        }),
        closeOnBackdropClick: false,
      });

      this.overlay = some(overlay);
    });
  }

  private closeOverlayIfOpen(): void {
    this.overlay.ifSome((overlay) => overlay.close());
    this.overlay = none();
  }
}
