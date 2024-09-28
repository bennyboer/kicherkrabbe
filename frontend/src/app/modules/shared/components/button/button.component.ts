import {
  booleanAttribute,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  HostBinding,
  HostListener,
  Input,
  OnDestroy,
  OnInit,
  Optional,
  Output,
  Renderer2,
} from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { ButtonId, ButtonRegistry } from './button-registry';
import { none, Option, some, someOrNone } from '../../modules/option';

export enum ButtonSize {
  SMALL = 'SMALL',
  NORMAL = 'MEDIUM',
  LARGE = 'LARGE',
  FIT_CONTENT = 'FIT_CONTENT',
}

@Component({
  selector: 'app-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '[attr.tabindex]': '0',
  },
})
export class ButtonComponent implements OnInit, OnDestroy {
  @HostBinding('class.active')
  @Input({ transform: booleanAttribute })
  active: boolean = false;

  @HostBinding('class.rounded')
  @Input({ transform: booleanAttribute })
  rounded: boolean = true;

  @HostBinding('class.disabled')
  @Input({ transform: booleanAttribute })
  disabled: boolean = false;

  @Input()
  set color(color: 'normal' | 'primary' | 'warn') {
    this.removeClass(color);
    this.addClass(color);
  }

  @Input('size')
  set setSize(size: ButtonSize) {
    this.sizeToClass(this.size).ifSome((className) =>
      this.removeClass(className),
    );
    this.sizeToClass(size).ifSome((className) => this.addClass(className));
    this.size = size;
  }

  @Output()
  click: EventEmitter<MouseEvent | KeyboardEvent> = new EventEmitter<
    MouseEvent | KeyboardEvent
  >();

  private size: ButtonSize = ButtonSize.NORMAL;
  private buttonId: Option<ButtonId> = none();
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    @Optional() private readonly buttonRegistry: ButtonRegistry,
    private readonly elementRef: ElementRef,
    private readonly renderer: Renderer2,
  ) {}

  ngOnInit(): void {
    someOrNone(this.buttonRegistry).ifSome((registry) => {
      const buttonId = registry.register(this);
      this.buttonId = some(buttonId);
      this.rounded = false;

      registry
        .isFirst(buttonId)
        .pipe(takeUntil(this.destroy$))
        .subscribe((rounded) => {
          if (rounded) {
            this.addClass('first-item');
          } else {
            this.removeClass('first-item');
          }
        });

      registry
        .isLast(buttonId)
        .pipe(takeUntil(this.destroy$))
        .subscribe((lastAndRounded) => {
          if (lastAndRounded) {
            this.addClass('last-item');
          } else {
            this.removeClass('last-item');
          }
        });

      registry
        .getSize()
        .pipe(takeUntil(this.destroy$))
        .subscribe((size) => {
          this.setSize = size;
        });
    });
  }

  ngOnDestroy(): void {
    this.buttonId.ifSome((id) => {
      this.buttonRegistry.unregister(id);
    });

    this.destroy$.next();
    this.destroy$.complete();
  }

  addClass(name: string): void {
    this.renderer.addClass(this.elementRef.nativeElement, name);
  }

  removeClass(name: string): void {
    this.renderer.removeClass(this.elementRef.nativeElement, name);
  }

  @HostListener('keydown', ['$event'])
  onEnter(event: KeyboardEvent): void {
    if (this.disabled) {
      return;
    }

    const isEnter = event.key === 'Enter';
    if (isEnter) {
      this.click.emit(event);
    }
  }

  private sizeToClass(size: ButtonSize): Option<string> {
    switch (size) {
      case ButtonSize.SMALL:
        return some('small');
      case ButtonSize.LARGE:
        return some('large');
      case ButtonSize.FIT_CONTENT:
        return some('fit-content');
      default:
        return none();
    }
  }
}
