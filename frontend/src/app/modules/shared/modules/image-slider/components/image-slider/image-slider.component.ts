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
import {
  BehaviorSubject,
  filter,
  Observable,
  ReplaySubject,
  Subject,
  takeUntil,
} from 'rxjs';
import { ImageSliderImage, Thumbnail } from '../../models';
import { someOrNone } from '../../../../../../util';
import { SlidingImageComponent } from '../sliding-image/sliding-image.component';

@Component({
  selector: 'app-image-slider',
  templateUrl: './image-slider.component.html',
  styleUrls: ['./image-slider.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImageSliderComponent implements OnInit, OnDestroy {
  @ViewChild(SlidingImageComponent)
  slidingImage!: SlidingImageComponent;

  protected readonly images$: BehaviorSubject<ImageSliderImage[]> =
    new BehaviorSubject<ImageSliderImage[]>([]);
  private readonly destroy$: Subject<void> = new Subject<void>();
  protected readonly thumbnails$: Subject<Thumbnail[]> = new ReplaySubject<
    Thumbnail[]
  >(1);
  protected readonly theme$: BehaviorSubject<'light' | 'dark'> =
    new BehaviorSubject<'light' | 'dark'>('light');
  protected readonly fit$: BehaviorSubject<'contain' | 'cover'> =
    new BehaviorSubject<'contain' | 'cover'>('contain');

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
      const resizeObserver = new ResizeObserver((entries) => {
        observer.next();
      });

      resizeObserver.observe(this.elementRef.nativeElement);

      return {
        unsubscribe: () => resizeObserver.disconnect(),
      };
    });

    resized$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.cd.markForCheck());

    this.images$
      .pipe(
        filter((images) => images.length > 0),
        takeUntil(this.destroy$),
      )
      .subscribe((images) => {
        this.thumbnails$.next(
          images.map((image, index) =>
            Thumbnail.of({ image, active: index === 0 }),
          ),
        );
      });
  }

  ngOnDestroy(): void {
    this.images$.complete();
    this.thumbnails$.complete();
    this.theme$.complete();
    this.fit$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  onThumbnailClicked(thumbnail: Thumbnail): void {
    const images = this.images$.value;
    const image = thumbnail.image;
    const imageIndex = images.findIndex(
      (image) => image.url === thumbnail.image.url,
    );

    this.slidingImage.slideTo(Math.max(0, imageIndex));
    this.thumbnails$.next(
      images.map((thumbnailImage) =>
        Thumbnail.of({
          image: thumbnailImage,
          active: thumbnailImage === image,
        }),
      ),
    );
  }

  onImageIndexChanged(index: number): void {
    const images = this.images$.value;
    const image = images[index];

    this.thumbnails$.next(
      images.map((thumbnailImage) =>
        Thumbnail.of({
          image: thumbnailImage,
          active: thumbnailImage === image,
        }),
      ),
    );
  }
}
