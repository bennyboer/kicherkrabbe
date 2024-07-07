import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { fromEvent, ReplaySubject, Subject, takeUntil } from 'rxjs';
import { ImageSliderImage } from '../../models';
import { none, Option, some, someOrNone } from '../../../../../../util';

@Component({
  selector: 'app-image-slider-image',
  templateUrl: './image.component.html',
  styleUrls: ['./image.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImageComponent implements OnInit, OnDestroy {
  protected readonly image$: Subject<ImageSliderImage> =
    new ReplaySubject<ImageSliderImage>(1);
  private readonly destroy$: Subject<void> = new Subject<void>();

  private dragStartX: number = 0;
  private dragging: boolean = false;
  private requestId: Option<number> = none();
  private lastDirection: Option<'previous' | 'next'> = none();
  private targetX: number = 0;

  @Input({ required: true })
  set image(value: ImageSliderImage) {
    someOrNone(value).map((image) => this.image$.next(image));
  }

  @Output()
  readonly dragOffsetChanged: EventEmitter<number> = new EventEmitter<number>();

  @Output()
  readonly draggingStarted: EventEmitter<void> = new EventEmitter<void>();

  @Output()
  readonly draggingEnded: EventEmitter<number> = new EventEmitter<number>();

  constructor(private readonly elementRef: ElementRef) {}

  ngOnInit(): void {
    fromEvent(this.elementRef.nativeElement, 'mousedown')
      .pipe(takeUntil(this.destroy$))
      .subscribe((event) => this.onMouseDown((event as MouseEvent).clientX));

    fromEvent(this.elementRef.nativeElement, 'touchstart')
      .pipe(takeUntil(this.destroy$))
      .subscribe((event) => {
        if ((event as TouchEvent).touches.length !== 1) {
          return;
        }

        this.onMouseDown((event as TouchEvent).touches[0].clientX);
      });

    fromEvent(window, 'mouseup')
      .pipe(takeUntil(this.destroy$))
      .subscribe((_event) => this.onMouseUp());

    fromEvent(window, 'touchend')
      .pipe(takeUntil(this.destroy$))
      .subscribe((_event) => this.onMouseUp());

    fromEvent(window, 'mousemove')
      .pipe(takeUntil(this.destroy$))
      .subscribe((event) => this.onMouseMove((event as MouseEvent).clientX));

    fromEvent(window, 'touchmove')
      .pipe(takeUntil(this.destroy$))
      .subscribe((event) => {
        if ((event as TouchEvent).touches.length !== 1) {
          return;
        }

        this.onMouseMove((event as TouchEvent).touches[0].clientX);
      });
  }

  ngOnDestroy(): void {
    this.image$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  onMouseDown(x: number): void {
    this.dragging = true;
    this.dragStartX = x;
    this.dragOffsetChanged.emit(0);
    this.draggingStarted.emit();
  }

  onMouseUp(): void {
    if (!this.dragging) {
      return;
    }

    this.dragging = false;
    this.dragStartX = 0;
    this.lastDirection = none();
    this.dragOffsetChanged.emit(0);
    this.draggingEnded.emit(this.targetX);
    this.targetX = 0;

    this.requestId.ifSome((id) => {
      window.cancelAnimationFrame(id);
      this.requestId = none();
    });
  }

  onMouseMove(x: number): void {
    if (!this.dragging) {
      return;
    }

    const diff = x - this.dragStartX;

    const width = this.elementRef.nativeElement.clientWidth;
    const switchedImage = Math.abs(diff) > width / 2;
    if (switchedImage) {
      const direction = diff > 0 ? 'previous' : 'next';
      this.lastDirection.ifSomeOrElse(
        (lastDirection) => {
          if (lastDirection !== direction) {
            this.lastDirection = some(direction);
            this.targetX = direction === 'previous' ? width : -width;
          }
        },
        () => {
          this.lastDirection = some(direction);
          this.targetX = direction === 'previous' ? width : -width;
        },
      );
    } else {
      this.targetX = 0;
    }

    this.requestId.ifSome((id) => {
      window.cancelAnimationFrame(id);
    });
    this.requestId = some(
      window.requestAnimationFrame(() => {
        this.dragOffsetChanged.emit(diff);
      }),
    );
  }
}
