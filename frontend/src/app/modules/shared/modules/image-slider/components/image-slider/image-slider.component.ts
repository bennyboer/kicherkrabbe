import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { BehaviorSubject, Observable, Subject, takeUntil } from 'rxjs';
import { ImageSliderImage } from '../../models';
import { SlidingImageComponent } from '../sliding-image/sliding-image.component';
import { someOrNone } from '../../../option';

@Component({
  selector: 'app-image-slider',
  templateUrl: './image-slider.component.html',
  styleUrls: ['./image-slider.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImageSliderComponent implements OnInit, OnDestroy {
  @ViewChild(SlidingImageComponent)
  slidingImage!: SlidingImageComponent;

  protected readonly images$: BehaviorSubject<ImageSliderImage[]> = new BehaviorSubject<ImageSliderImage[]>([]);
  private readonly destroy$: Subject<void> = new Subject<void>();
  protected readonly theme$: BehaviorSubject<'light' | 'dark'> = new BehaviorSubject<'light' | 'dark'>('light');
  protected readonly fit$: BehaviorSubject<'contain' | 'cover'> = new BehaviorSubject<'contain' | 'cover'>('contain');
  protected readonly activeImageIndex$: BehaviorSubject<number> = new BehaviorSubject<number>(0);

  @Input({ required: true })
  set images(value: ImageSliderImage[]) {
    someOrNone(value).map((images) => this.images$.next(images));
  }

  @Input()
  set fit(value: 'contain' | 'cover') {
    someOrNone(value).map((fit) => this.fit$.next(fit));
  }

  @Input()
  set theme(value: 'light' | 'dark' | null) {
    someOrNone(value).map((theme) => this.theme$.next(theme));
  }

  constructor(
    private readonly elementRef: ElementRef,
    private readonly cd: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const resized$ = new Observable<void>((observer) => {
      const resizeObserver = new ResizeObserver(() => {
        observer.next();
      });

      resizeObserver.observe(this.elementRef.nativeElement);

      return {
        unsubscribe: () => resizeObserver.disconnect(),
      };
    });

    resized$.pipe(takeUntil(this.destroy$)).subscribe(() => this.cd.markForCheck());
  }

  ngOnDestroy(): void {
    this.images$.complete();
    this.theme$.complete();
    this.fit$.complete();
    this.activeImageIndex$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  onThumbnailClicked(image: ImageSliderImage): void {
    const images = this.images$.value;
    const imageIndex = images.findIndex((i) => i.url === image.url);

    this.slidingImage.slideTo(Math.max(0, imageIndex));
  }

  onImageIndexChanged(index: number): void {
    this.activeImageIndex$.next(index);
  }
}
