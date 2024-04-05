import {
  booleanAttribute,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostBinding,
  Input,
  OnDestroy,
  OnInit,
  Optional,
  Renderer2,
} from '@angular/core';
import { Option } from '../../../../util';
import { Subject, takeUntil } from 'rxjs';
import { ButtonId, ButtonRegistry } from './button-registry';

export enum Size {
  SMALL = 'SMALL',
  NORMAL = 'MEDIUM',
  LARGE = 'LARGE',
}

@Component({
  selector: 'app-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ButtonComponent implements OnInit, OnDestroy {
  @HostBinding('class.active')
  @Input({ transform: booleanAttribute })
  active: boolean = false;

  @HostBinding('class.rounded')
  @Input({ transform: booleanAttribute })
  rounded: boolean = true;

  @Input('size')
  set setSize(size: Size) {
    this.sizeToClass(this.size).ifSome((className) =>
      this.removeClass(className),
    );
    this.sizeToClass(size).ifSome((className) => this.addClass(className));
    this.size = size;
  }

  private size: Size = Size.NORMAL;
  private buttonId: Option<ButtonId> = Option.none();
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    @Optional() private readonly buttonRegistry: ButtonRegistry,
    private readonly elementRef: ElementRef,
    private readonly renderer: Renderer2,
    private readonly cd: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    Option.someOrNone(this.buttonRegistry).ifSome((registry) => {
      const buttonId = registry.register(this);
      this.buttonId = Option.some(buttonId);
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

  private sizeToClass(size: Size): Option<string> {
    switch (size) {
      case Size.SMALL:
        return Option.some('small');
      case Size.LARGE:
        return Option.some('large');
      default:
        return Option.none();
    }
  }
}
