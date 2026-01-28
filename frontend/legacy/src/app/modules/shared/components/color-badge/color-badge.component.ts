import {
  booleanAttribute,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Input,
  numberAttribute,
  OnDestroy,
  OnInit,
  Renderer2,
  RendererStyleFlags2,
} from '@angular/core';
import { BehaviorSubject, Subject, takeUntil } from 'rxjs';
import { someOrNone } from '../../modules/option';

export interface ColorBadgeColor {
  red: number;
  green: number;
  blue: number;
}

const DEFAULT_COLOR: ColorBadgeColor = {
  red: 255,
  green: 0,
  blue: 0,
};

@Component({
  selector: 'app-color-badge',
  templateUrl: './color-badge.component.html',
  styleUrls: ['./color-badge.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class ColorBadgeComponent implements OnInit, OnDestroy {
  private readonly color$: BehaviorSubject<ColorBadgeColor> = new BehaviorSubject<ColorBadgeColor>(DEFAULT_COLOR);
  private readonly size$: BehaviorSubject<number> = new BehaviorSubject<number>(32);
  private readonly destroy$: Subject<void> = new Subject<void>();

  @Input()
  set color(value: ColorBadgeColor | null) {
    someOrNone(value).ifSome((color) => this.color$.next(color));
  }

  @Input({ transform: numberAttribute })
  set size(value: number) {
    this.size$.next(value);
  }

  @Input({ transform: booleanAttribute })
  set clickable(value: boolean) {
    if (value) {
      this.renderer.addClass(this.elementRef.nativeElement, 'clickable');
    } else {
      this.renderer.removeClass(this.elementRef.nativeElement, 'clickable');
    }
  }

  constructor(
    private readonly elementRef: ElementRef,
    private readonly renderer: Renderer2,
  ) {}

  ngOnInit(): void {
    this.size$.pipe(takeUntil(this.destroy$)).subscribe((size) => {
      this.renderer.setStyle(this.elementRef.nativeElement, '--size', `${size}px`, RendererStyleFlags2.DashCase);
    });

    this.color$.pipe(takeUntil(this.destroy$)).subscribe((color) => {
      this.renderer.setStyle(
        this.elementRef.nativeElement,
        '--color',
        `${color.red}, ${color.green}, ${color.blue}`,
        RendererStyleFlags2.DashCase,
      );
    });
  }

  ngOnDestroy(): void {
    this.color$.complete();
    this.size$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }
}
