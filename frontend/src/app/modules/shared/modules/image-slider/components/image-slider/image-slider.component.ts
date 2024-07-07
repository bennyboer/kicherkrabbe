import {
  ChangeDetectionStrategy,
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
  first,
  fromEvent,
  ReplaySubject,
  Subject,
  takeUntil,
} from 'rxjs';
import { ImageSliderImage, Thumbnail } from '../../models';
import { none, Option, some, someOrNone } from '../../../../../../util';

@Component({
  selector: 'app-image-slider',
  templateUrl: './image-slider.component.html',
  styleUrls: ['./image-slider.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImageSliderComponent implements OnInit, OnDestroy {
  @ViewChild('images')
  imagesElementRef!: ElementRef<HTMLDivElement>;

  private readonly images$: BehaviorSubject<ImageSliderImage[]> =
    new BehaviorSubject<ImageSliderImage[]>([]);
  private readonly destroy$: Subject<void> = new Subject<void>();
  protected readonly image$: Subject<ImageSliderImage> =
    new ReplaySubject<ImageSliderImage>(1);
  protected readonly nextImage$: BehaviorSubject<Option<ImageSliderImage>> =
    new BehaviorSubject<Option<ImageSliderImage>>(none());
  protected readonly previousImage$: BehaviorSubject<Option<ImageSliderImage>> =
    new BehaviorSubject<Option<ImageSliderImage>>(none());
  protected readonly thumbnails$: Subject<Thumbnail[]> = new ReplaySubject<
    Thumbnail[]
  >(1);
  protected readonly dragOffset$: BehaviorSubject<number> =
    new BehaviorSubject<number>(0);
  protected readonly dragging$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  protected readonly animationsDisabled$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);

  @Input({ required: true })
  set images(value: ImageSliderImage[]) {
    someOrNone(value).map((images) => this.images$.next(images));
  }

  ngOnInit(): void {
    this.images$
      .pipe(
        filter((images) => images.length > 0),
        takeUntil(this.destroy$),
      )
      .subscribe((images) => {
        this.image$.next(images[0]);
        this.thumbnails$.next(
          images.map((image, index) =>
            Thumbnail.of({ image, active: index === 0 }),
          ),
        );

        if (images.length > 1) {
          this.nextImage$.next(some(images[1]));
        } else {
          this.nextImage$.next(none());
        }
      });
  }

  ngOnDestroy(): void {
    this.images$.complete();
    this.image$.complete();
    this.thumbnails$.complete();
    this.nextImage$.complete();
    this.previousImage$.complete();
    this.dragOffset$.complete();
    this.dragging$.complete();
    this.animationsDisabled$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  onDragOffsetChanged(offset: number): void {
    if (this.dragging$.value) {
      this.dragOffset$.next(offset);
    }
  }

  onDraggingStarted(): void {
    this.dragging$.next(true);
    this.animationsDisabled$.next(true);
  }

  onDraggingEnded(targetX: number): void {
    this.dragging$.next(false);
    this.animationsDisabled$.next(false);

    if (targetX !== 0) {
      if (targetX < 0) {
        if (this.nextImage$.value.isSome()) {
          this.dragOffset$.next(targetX);
        }
      } else if (targetX > 0) {
        if (this.previousImage$.value.isSome()) {
          this.dragOffset$.next(targetX);
        }
      }

      fromEvent(this.imagesElementRef.nativeElement, 'transitionend')
        .pipe(first(), takeUntil(this.destroy$))
        .subscribe(() => {
          const images = this.images$.value;

          if (targetX < 0) {
            if (this.nextImage$.value.isSome()) {
              const nextImage = this.nextImage$.value.orElseThrow();
              const nextImageIndex = images.indexOf(nextImage);
              const newNextImageIndex = nextImageIndex + 1;
              if (newNextImageIndex < images.length) {
                this.nextImage$.next(some(images[newNextImageIndex]));
              } else {
                this.nextImage$.next(none());
              }
              const newPreviousImageIndex = nextImageIndex - 1;
              this.previousImage$.next(some(images[newPreviousImageIndex]));

              this.image$.next(nextImage);
              this.animationsDisabled$.next(true);
              this.thumbnails$.next(
                images.map((thumbnailImage) =>
                  Thumbnail.of({
                    image: thumbnailImage,
                    active: thumbnailImage === nextImage,
                  }),
                ),
              );
              this.dragOffset$.next(0);
            } else {
              this.dragOffset$.next(0);
            }
          } else if (targetX > 0) {
            if (this.previousImage$.value.isSome()) {
              const previousImage = this.previousImage$.value.orElseThrow();
              const previousImageIndex = images.indexOf(previousImage);
              const newPreviousImageIndex = previousImageIndex - 1;
              if (newPreviousImageIndex >= 0) {
                this.previousImage$.next(some(images[newPreviousImageIndex]));
              } else {
                this.previousImage$.next(none());
              }
              const newNextImageIndex = previousImageIndex + 1;
              this.nextImage$.next(some(images[newNextImageIndex]));

              this.image$.next(previousImage);
              this.animationsDisabled$.next(true);
              this.thumbnails$.next(
                images.map((thumbnailImage) =>
                  Thumbnail.of({
                    image: thumbnailImage,
                    active: thumbnailImage === previousImage,
                  }),
                ),
              );
              this.dragOffset$.next(0);
            } else {
              this.dragOffset$.next(0);
            }
          }
        });
    }
  }

  onThumbnailClicked(thumbnail: Thumbnail): void {
    const images = this.images$.value;
    const image = thumbnail.image;
    const imageIndex = images.indexOf(image);
    const previousImageIndex = imageIndex - 1;
    const nextImageIndex = imageIndex + 1;

    if (previousImageIndex >= 0) {
      this.previousImage$.next(some(images[previousImageIndex]));
    } else {
      this.previousImage$.next(none());
    }

    if (nextImageIndex < images.length) {
      this.nextImage$.next(some(images[nextImageIndex]));
    } else {
      this.nextImage$.next(none());
    }

    this.image$.next(image);
    this.dragOffset$.next(0);
    this.animationsDisabled$.next(true);
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
