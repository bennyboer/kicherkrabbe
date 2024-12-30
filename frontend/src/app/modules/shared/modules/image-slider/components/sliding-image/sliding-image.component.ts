import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  QueryList,
  Renderer2,
  ViewChildren,
} from '@angular/core';
import { ImageSliderImage } from '../../models';
import {
  animationFrameScheduler,
  BehaviorSubject,
  combineLatest,
  concatMap,
  debounceTime,
  delay,
  distinctUntilChanged,
  filter,
  from,
  fromEvent,
  map,
  Observable,
  startWith,
  Subject,
  switchMap,
  takeUntil,
  tap,
  toArray,
} from 'rxjs';
import { none, Option, some, someOrNone } from '../../../option';

const LOADING_ANIMATION_DURATION_MS = 2000;
const easeInOut = (progress: number) =>
  progress < 0.5 ? 4 * progress * progress * progress : (progress - 1) * (2 * progress - 2) * (2 * progress - 2) + 1;

type ImageFit = 'contain' | 'cover';

interface ImageLayout {
  width: number;
  height: number;
  x: number;
  y: number;
}

@Component({
  selector: 'app-sliding-image',
  templateUrl: 'sliding-image.component.html',
  styleUrls: ['sliding-image.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SlidingImageComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChildren('canvas')
  canvasElements!: QueryList<ElementRef>;

  @Input()
  set images(value: ImageSliderImage[] | null) {
    someOrNone(value).ifSome((images) => this.images$.next(images));
  }

  @Input()
  set width(value: number | null) {
    someOrNone(value).ifSome((width) => this.width$.next(width));
  }

  @Input()
  set height(value: number | null) {
    someOrNone(value).ifSome((height) => this.height$.next(height));
  }

  @Input()
  set theme(value: 'light' | 'dark' | null) {
    someOrNone(value).ifSome((theme) => this.theme$.next(theme));
  }

  @Input()
  set fit(value: ImageFit | null) {
    someOrNone(value).ifSome((fit) => this.fit$.next(fit));
  }

  @Output()
  imageIndexChanged: EventEmitter<number> = new EventEmitter<number>();

  private readonly dragging$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly imageIndex$: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  private readonly offset$: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  private readonly fit$: BehaviorSubject<ImageFit> = new BehaviorSubject<ImageFit>('contain');
  private readonly theme$: BehaviorSubject<'light' | 'dark'> = new BehaviorSubject<'light' | 'dark'>('light');
  private readonly width$: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  private readonly height$: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  private readonly images$: BehaviorSubject<ImageSliderImage[]> = new BehaviorSubject<ImageSliderImage[]>([]);
  private readonly loadedImages$: BehaviorSubject<HTMLImageElement[]> = new BehaviorSubject<HTMLImageElement[]>([]);
  protected readonly loading$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly canvas$: BehaviorSubject<Option<HTMLCanvasElement>> = new BehaviorSubject<Option<HTMLCanvasElement>>(
    none(),
  );
  private readonly repaint$: Subject<void> = new Subject<void>();
  private readonly destroy$: Subject<void> = new Subject<void>();

  private dragStartX: number = 0;
  private offsetBeforeDragging: number = 0;
  private dragStartTimestamp: number = 0;

  private resetAnimationOngoing: boolean = false;
  private resetAnimationStartX: number = 0;
  private resetAnimationEndX: number = 0;
  private resetAnimationStartTimestamp: number = window.performance.now();
  private resetAnimationDurationMs: number = 0;

  constructor(
    private readonly elementRef: ElementRef,
    private readonly renderer: Renderer2,
  ) {}

  ngOnInit(): void {
    this.images$
      .pipe(
        distinctUntilChanged((a, b) => {
          if (a.length !== b.length) {
            return true;
          }

          const beforeSet = new Set<string>(a.map((i) => i.url));
          return a.some((i) => !beforeSet.has(i.url));
        }),
        tap(() => this.loading$.next(true)),
        switchMap((images) =>
          from(images).pipe(
            concatMap((image) => this.loadImage(image)),
            toArray(),
          ),
        ),
        delay(500),
        tap((images) => {
          this.loading$.next(false);
          this.loadedImages$.next(images);
        }),
        takeUntil(this.destroy$),
      )
      .subscribe();

    const canvasReady$ = this.canvas$.pipe(
      filter((canvas) => canvas.isSome()),
      map((canvas) => canvas.orElseThrow()),
    );

    combineLatest([
      canvasReady$,
      this.loadedImages$,
      this.loading$,
      this.width$,
      this.height$,
      this.theme$,
      this.fit$,
      this.offset$,
    ])
      .pipe(
        tap(() => this.scheduleRepaint()),
        takeUntil(this.destroy$),
      )
      .subscribe();

    const repaintScheduled$ = this.repaint$;
    repaintScheduled$
      .pipe(debounceTime(0, animationFrameScheduler), takeUntil(this.destroy$))
      .subscribe(() => this.repaint());

    this.height$
      .pipe(takeUntil(this.destroy$))
      .subscribe((height) => this.renderer.setStyle(this.elementRef.nativeElement, 'height', `${height}px`));

    this.dragging$.pipe(takeUntil(this.destroy$)).subscribe((dragging) => {
      if (dragging) {
        this.renderer.addClass(this.elementRef.nativeElement, 'dragging');
      } else {
        this.renderer.removeClass(this.elementRef.nativeElement, 'dragging');
      }
    });

    this.imageIndex$
      .pipe(distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe((imageIndex) => this.imageIndexChanged.emit(imageIndex));

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
      .subscribe((event) => this.onMouseUp((event as MouseEvent).clientX));

    fromEvent(window, 'touchend')
      .pipe(takeUntil(this.destroy$))
      .subscribe((event) => {
        if ((event as TouchEvent).changedTouches.length !== 1) {
          return;
        }

        this.onMouseUp((event as TouchEvent).changedTouches[0].clientX);
      });

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

  ngAfterViewInit(): void {
    this.canvasElements.changes
      .pipe(startWith(this.canvasElements), takeUntil(this.destroy$))
      .subscribe((canvasElements) => {
        if (canvasElements.length === 0) {
          return;
        }

        const canvasElement = canvasElements.first.nativeElement;
        this.canvas$.next(some(canvasElement));
      });
  }

  ngOnDestroy(): void {
    this.images$.complete();
    this.loading$.complete();
    this.loadedImages$.complete();
    this.canvas$.complete();
    this.width$.complete();
    this.height$.complete();
    this.repaint$.complete();
    this.theme$.complete();
    this.offset$.complete();
    this.imageIndex$.complete();
    this.dragging$.complete();
    this.fit$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  slideTo(imageIndex: number): void {
    this.imageIndex$.next(imageIndex);

    const nearestImageOffset = -imageIndex * this.width$.value;
    if (this.offset$.value === nearestImageOffset) {
      this.offset$.next(nearestImageOffset);
    } else {
      this.resetAnimationOngoing = true;
      this.resetAnimationStartX = this.offset$.value;
      this.resetAnimationEndX = nearestImageOffset;
      this.resetAnimationStartTimestamp = window.performance.now();
      this.resetAnimationDurationMs = 300;
      this.scheduleRepaint();
    }
  }

  private loadImage(image: ImageSliderImage): Observable<HTMLImageElement> {
    return new Observable<HTMLImageElement>((observer) => {
      const imageElement = new Image();
      imageElement.src = image.url;
      imageElement.onload = () => {
        observer.next(imageElement);
        observer.complete();
      };
    });
  }

  private repaint(): void {
    const timestamp = window.performance.now();

    const canvas = this.canvas$.value.orElseThrow('Canvas unavailable');
    const ctx = someOrNone(canvas.getContext('2d')).orElseThrow('Canvas 2D context unavailable');

    this.resizeCanvasIfNecessary(canvas);

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const isLoading = this.loading$.value;
    if (isLoading) {
      this.renderLoading(ctx, timestamp);
    } else {
      this.renderImages(ctx, timestamp);
    }

    const isStable = !this.resetAnimationOngoing;
    const isPanning = false; // TODO Should be true if the image is currently panning
    const isAnimating = isLoading || isPanning || !isStable;
    if (isAnimating) {
      this.scheduleRepaint();
    }
  }

  private resizeCanvasIfNecessary(canvas: HTMLCanvasElement): void {
    const devicePixelRatio = window.devicePixelRatio || 1;

    const actualWidth = canvas.width;
    const actualHeight = canvas.height;

    const expectedWidth = Math.round(this.width$.value * devicePixelRatio);
    const expectedHeight = Math.round(this.height$.value * devicePixelRatio);

    const needsResize = actualWidth !== expectedWidth || actualHeight !== expectedHeight;
    if (needsResize) {
      canvas.width = expectedWidth;
      canvas.height = expectedHeight;

      canvas.style.width = `${Math.round(this.width$.value)}px`;
      canvas.style.height = `${Math.round(this.height$.value)}px`;

      this.offset$.next(-this.imageIndex$.value * this.width$.value);
    }
  }

  private renderLoading(ctx: CanvasRenderingContext2D, timestamp: number): void {
    const animationProgress = (timestamp % LOADING_ANIMATION_DURATION_MS) / LOADING_ANIMATION_DURATION_MS;
    const curvedAnimationProgress = easeInOut(animationProgress);

    const bouncyProgress = Math.sin(curvedAnimationProgress * Math.PI);
    const minTransparency = 0.2;
    const transparency = minTransparency + bouncyProgress * (1 - minTransparency);

    const width = ctx.canvas.width;
    const height = ctx.canvas.height;

    const greyTone = this.theme$.value === 'light' ? 200 : 70;
    ctx.fillStyle = `rgba(${greyTone}, ${greyTone}, ${greyTone}, ${transparency})`;
    ctx.fillRect(0, 0, width, height);
  }

  private renderImages(ctx: CanvasRenderingContext2D, timestamp: number): void {
    const images = this.loadedImages$.value;

    const width = ctx.canvas.width;
    const height = ctx.canvas.height;

    if (this.resetAnimationOngoing) {
      const progress = easeInOut((timestamp - this.resetAnimationStartTimestamp) / this.resetAnimationDurationMs);

      if (progress >= 1.0) {
        this.resetAnimationOngoing = false;
        this.offset$.next(this.resetAnimationEndX);
      } else {
        this.offset$.next(this.resetAnimationStartX + (this.resetAnimationEndX - this.resetAnimationStartX) * progress);
      }
    }

    const devicePixelRatio = window.devicePixelRatio || 1;
    const offset = this.offset$.value * devicePixelRatio;
    const fit = this.fit$.value;

    for (let i = 0; i < images.length; i++) {
      const image = images[i];
      const x = offset + i * width;

      const layout = this.layoutImage({
        width,
        height,
        image,
        fit,
      });

      const isInViewport = x + layout.width > 0 && x < width;
      if (isInViewport) {
        ctx.drawImage(image, x + layout.x, layout.y, layout.width, layout.height);
      }
    }
  }

  private layoutImage(props: { width: number; height: number; image: HTMLImageElement; fit: ImageFit }): ImageLayout {
    if (props.fit === 'contain') {
      return this.layoutContainedImage({
        width: props.width,
        height: props.height,
        image: props.image,
      });
    } else {
      return this.layoutCoveredImage({
        width: props.width,
        height: props.height,
        image: props.image,
      });
    }
  }

  private layoutContainedImage(props: { width: number; height: number; image: HTMLImageElement }): ImageLayout {
    const { width, height, image } = props;

    const preferredAspectRatio = width / height;
    const imageAspectRatio = image.width / image.height;
    const isImageWider = imageAspectRatio >= preferredAspectRatio;
    if (isImageWider) {
      const aspectRatio = image.height / image.width;
      const imageWidth = width;
      const imageHeight = width * aspectRatio;
      const x = 0;
      const y = (height - imageHeight) / 2;
      return { width: imageWidth, height: imageHeight, x, y };
    } else {
      const aspectRatio = image.width / image.height;
      const imageWidth = height * aspectRatio;
      const imageHeight = height;
      const x = (width - imageWidth) / 2;
      const y = 0;
      return { width: imageWidth, height: imageHeight, x, y };
    }
  }

  private layoutCoveredImage(props: { width: number; height: number; image: HTMLImageElement }): ImageLayout {
    const { width, height, image } = props;

    const isPortrait = image.height > image.width;
    let imageWidth = width;
    let imageHeight = height;
    let x = 0;
    let y = 0;
    if (isPortrait) {
      const aspectRatio = image.width / image.height;
      imageWidth = width;
      imageHeight = width / aspectRatio;
      y = (height - imageHeight) / 2;
    } else {
      const aspectRatio = image.height / image.width;
      imageWidth = height / aspectRatio;
      imageHeight = height;
      x = (width - imageWidth) / 2;
    }

    return {
      width: imageWidth,
      height: imageHeight,
      x,
      y,
    };
  }

  private scheduleRepaint(): void {
    this.repaint$.next();
  }

  private onMouseDown(x: number): void {
    if (this.loading$.value) {
      return;
    }

    this.dragging$.next(true);
    this.dragStartX = x;
    this.dragStartTimestamp = window.performance.now();

    if (this.resetAnimationOngoing) {
      this.resetAnimationOngoing = false;
      this.offsetBeforeDragging = this.resetAnimationEndX;
    } else {
      this.offsetBeforeDragging = this.offset$.value;
    }
  }

  private onMouseUp(x: number): void {
    if (!this.dragging$.value) {
      return;
    }

    let nextImageIndex = this.findNearestImageIndex(this.offset$.value);
    const imageIndexChanged = nextImageIndex !== this.imageIndex$.value;
    if (!imageIndexChanged) {
      const totalDragDistance = x - this.dragStartX;
      const totalDragDistanceInPercent = Math.abs(totalDragDistance) / this.width$.value;
      if (totalDragDistanceInPercent > 0.1) {
        const dragTime = window.performance.now() - this.dragStartTimestamp;
        const dragSpeed = Math.abs(totalDragDistanceInPercent / dragTime);
        if (dragSpeed > 0.001) {
          const direction = totalDragDistance > 0 ? -1 : 1;
          nextImageIndex += direction;

          const maxIndex = this.loadedImages$.value.length - 1;
          nextImageIndex = Math.max(0, Math.min(maxIndex, nextImageIndex));
        }
      }
    }

    this.slideTo(nextImageIndex);

    this.dragging$.next(false);
    this.dragStartX = 0;
  }

  private onMouseMove(x: number): void {
    if (!this.dragging$.value) {
      return;
    }

    const diff = x - this.dragStartX;
    const newOffset = this.offsetBeforeDragging + diff;
    if (newOffset !== this.offset$.value) {
      this.offset$.next(newOffset);
    }
  }

  private findNearestImageIndex(x: number): number {
    const width = this.width$.value;

    const images = this.loadedImages$.value;
    const imageCount = images.length;
    if (imageCount === 0) {
      return 0;
    }

    let nearestImageDistance = Number.MAX_VALUE;
    let nearestImageIndex = 0;
    for (let i = 0; i < imageCount; i++) {
      const imageX = i * width;
      const imageDistance = Math.abs(x + imageX);
      if (imageDistance < nearestImageDistance) {
        nearestImageDistance = imageDistance;
        nearestImageIndex = i;
      }
    }

    return nearestImageIndex;
  }
}
