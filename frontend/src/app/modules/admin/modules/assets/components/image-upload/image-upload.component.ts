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
  ViewChildren,
} from '@angular/core';
import { AssetsService } from '../../services/assets.service';
import {
  BehaviorSubject,
  combineLatest,
  concatMap,
  delay,
  from,
  map,
  mergeMap,
  Observable,
  ReplaySubject,
  Subject,
  switchMap,
  take,
  takeUntil,
  toArray,
} from 'rxjs';

type Step = 'file-select' | 'preview' | 'upload';

interface SelectedImageIndexContainer {
  index: number;
}

@Component({
  selector: 'app-image-upload',
  templateUrl: './image-upload.component.html',
  styleUrls: ['./image-upload.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImageUploadComponent implements OnInit, OnDestroy, AfterViewInit {
  @ViewChildren('canvas')
  canvasElements!: QueryList<ElementRef>;

  @Input()
  desiredWidth: number = 768;

  @Input()
  watermark: boolean = true;

  @Input()
  watermarkScale: number = 0.2;

  @Input()
  exportQuality: number = 0.9;

  @Input()
  multiple: boolean = false;

  @Output()
  uploaded: EventEmitter<string[]> = new EventEmitter<string[]>();

  private readonly step$: BehaviorSubject<Step> = new BehaviorSubject<Step>(
    'file-select',
  );
  private readonly files$: Subject<File[]> = new ReplaySubject(1);
  protected readonly images$: Subject<HTMLImageElement[]> = new ReplaySubject(
    1,
  );
  protected readonly selectedImageIndex$: BehaviorSubject<SelectedImageIndexContainer> =
    new BehaviorSubject<SelectedImageIndexContainer>({ index: 0 });
  private readonly blackWatermark$: Subject<HTMLImageElement> =
    new ReplaySubject(1);
  private readonly whiteWatermark$: Subject<HTMLImageElement> =
    new ReplaySubject(1);
  private readonly canvas$: Subject<HTMLCanvasElement> = new ReplaySubject(1);
  private readonly resultImages$: Subject<Blob[]> = new ReplaySubject(1);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(private readonly assetsService: AssetsService) {}

  ngOnInit(): void {
    this.files$
      .pipe(takeUntil(this.destroy$))
      .subscribe((files) => this.loadImagesFromFiles(files));

    const selectedImage$ = combineLatest([
      this.images$,
      this.selectedImageIndex$,
    ]).pipe(map(([images, index]) => images[index.index]));

    combineLatest([
      selectedImage$,
      this.blackWatermark$,
      this.whiteWatermark$,
      this.canvas$,
    ])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([image, blackWatermark, whiteWatermark, canvas]) =>
        this.onImageAndCanvasReady(
          image,
          blackWatermark,
          whiteWatermark,
          canvas,
        ),
      );

    combineLatest([this.images$, this.blackWatermark$, this.whiteWatermark$])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([images, blackWatermark, whiteWatermark]) =>
        this.renderImages(images, blackWatermark, whiteWatermark),
      );
  }

  ngAfterViewInit(): void {
    this.canvasElements.changes
      .pipe(takeUntil(this.destroy$))
      .subscribe((canvasElements) => {
        if (canvasElements.length === 0) {
          return;
        }

        const canvasElement = canvasElements.first.nativeElement;
        this.canvas$.next(canvasElement);
      });

    this.loadWatermarks();
  }

  ngOnDestroy(): void {
    this.step$.complete();
    this.files$.complete();
    this.images$.complete();
    this.blackWatermark$.complete();
    this.whiteWatermark$.complete();
    this.canvas$.complete();
    this.resultImages$.complete();
    this.selectedImageIndex$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getStep(): Observable<Step> {
    return this.step$.asObservable();
  }

  onFilesSelected(files: File[]): void {
    this.files$.next(files);
    this.step$.next('preview');
  }

  reset(): void {
    this.step$.next('file-select');
  }

  upload(): void {
    this.step$.next('upload');

    this.resultImages$
      .pipe(
        switchMap((blobs) =>
          from(blobs).pipe(
            mergeMap((blob) => this.assetsService.uploadAsset(blob)),
            take(blobs.length),
            toArray(),
          ),
        ),
        delay(2000),
        takeUntil(this.destroy$),
      )
      .subscribe((assetIds) => this.uploaded.emit(assetIds));
  }

  selectImageIndex(index: number, images: HTMLImageElement[]): void {
    if (index < 0 || index >= images.length) {
      return;
    }

    this.selectedImageIndex$.next({ index });
  }

  private renderImages(
    images: HTMLImageElement[],
    blackWatermark: HTMLImageElement,
    whiteWatermark: HTMLImageElement,
  ): void {
    from(images)
      .pipe(
        concatMap((image) =>
          this.renderImage(image, blackWatermark, whiteWatermark),
        ),
        toArray(),
        takeUntil(this.destroy$),
      )
      .subscribe((blobs) => this.resultImages$.next(blobs));
  }

  private onImageAndCanvasReady(
    image: HTMLImageElement,
    blackWatermark: HTMLImageElement,
    whiteWatermark: HTMLImageElement,
    canvas: HTMLCanvasElement,
  ): void {
    const context = canvas.getContext('2d');
    if (!context) {
      return;
    }

    const parentElement = canvas.parentElement;
    if (!parentElement) {
      return;
    }

    const parentWidth = parentElement.clientWidth;

    const devicePixelRatio = window.devicePixelRatio || 1;
    const aspectRatio = image.naturalWidth / image.naturalHeight;
    const width = Math.min(image.naturalWidth, parentWidth);
    const height = width / aspectRatio;

    this.drawImageAndWatermark(
      canvas,
      image,
      blackWatermark,
      whiteWatermark,
      width,
      height,
      devicePixelRatio,
    );
  }

  private dataUrlToBlob(dataUrl: string): Blob {
    const byteString = atob(dataUrl.split(',')[1]);
    const mimeString = dataUrl.split(',')[0].split(':')[1].split(';')[0];
    const buffer = new ArrayBuffer(byteString.length);
    const byteArray = new Uint8Array(buffer);
    for (let i = 0; i < byteString.length; i++) {
      byteArray[i] = byteString.charCodeAt(i);
    }

    return new Blob([buffer], { type: mimeString });
  }

  private drawImageAndWatermark(
    canvas: HTMLCanvasElement,
    image: HTMLImageElement,
    blackWatermark: HTMLImageElement,
    whiteWatermark: HTMLImageElement,
    width: number,
    height: number,
    devicePixelRatio: number,
  ): void {
    const context = canvas.getContext('2d');
    if (!context) {
      return;
    }

    canvas.style.width = `${width}px`;
    canvas.style.height = `${height}px`;

    width *= devicePixelRatio;
    height *= devicePixelRatio;

    canvas.width = width;
    canvas.height = height;

    context.drawImage(image, 0, 0, width, height);

    if (this.watermark) {
      const watermarkAspectRatio =
        blackWatermark.naturalWidth / blackWatermark.naturalHeight;

      const watermarkPaddingRatio = 0.02;
      const watermarkPadding = width * watermarkPaddingRatio;

      const watermarkWidth = width * this.watermarkScale;
      const watermarkHeight = watermarkWidth / watermarkAspectRatio;
      const x = width - watermarkWidth - watermarkPadding;
      const y = height - watermarkHeight - watermarkPadding;

      const darkness = this.getImageDarknessInArea(
        context,
        x,
        y,
        watermarkWidth,
        watermarkHeight,
      );
      const useWhiteWatermark = darkness < 80;
      const watermarkImage = useWhiteWatermark
        ? whiteWatermark
        : blackWatermark;
      context.drawImage(watermarkImage, x, y, watermarkWidth, watermarkHeight);
    }
  }

  private getImageDarknessInArea(
    context: CanvasRenderingContext2D,
    x: number,
    y: number,
    width: number,
    height: number,
  ): number {
    const imageData = context.getImageData(x, y, width, height);
    const data = imageData.data;

    let colorSum = 0;
    for (let i = 0; i < data.length; i += 4) {
      const r = data[i];
      const g = data[i + 1];
      const b = data[i + 2];

      const average = Math.floor((r + g + b) / 3);
      colorSum += average;
    }

    return colorSum / (data.length / 4);
  }

  private loadImagesFromFiles(files: File[]): void {
    if (files.length === 0) {
      return;
    }

    from(files)
      .pipe(
        concatMap((file) => this.loadImageFromFile(file)),
        toArray(),
      )
      .subscribe((images) => this.images$.next(images));
  }

  private loadImageFromFile(file: File): Observable<HTMLImageElement> {
    return new Observable<HTMLImageElement>((observer) => {
      const reader = new FileReader();
      reader.onload = (event) => {
        const image = new Image();
        image.onload = () => {
          observer.next(image);
          observer.complete();
        };
        image.src = event.target?.result as string;
      };
      reader.readAsDataURL(file);
    });
  }

  private loadWatermarks(): void {
    this.loadImageFromUrl('/assets/kicherkrabbe_outlines_black.svg')
      .pipe(takeUntil(this.destroy$))
      .subscribe((image) => this.blackWatermark$.next(image));
    this.loadImageFromUrl('/assets/kicherkrabbe_outlines_white.svg')
      .pipe(takeUntil(this.destroy$))
      .subscribe((image) => this.whiteWatermark$.next(image));
  }

  private loadImageFromUrl(url: string): Observable<HTMLImageElement> {
    return new Observable<HTMLImageElement>((observer) => {
      const image = new Image();
      image.onload = () => observer.next(image);
      image.src = url;
    });
  }

  private renderImage(
    image: HTMLImageElement,
    blackWatermark: HTMLImageElement,
    whiteWatermark: HTMLImageElement,
  ): Observable<Blob> {
    return new Observable<Blob>((observer) => {
      const aspectRatio = image.naturalWidth / image.naturalHeight;
      const resultCanvas = document.createElement('canvas');
      const desiredHeight = this.desiredWidth / aspectRatio;
      this.drawImageAndWatermark(
        resultCanvas,
        image,
        blackWatermark,
        whiteWatermark,
        this.desiredWidth,
        desiredHeight,
        1,
      );
      const dataUrl = resultCanvas.toDataURL('image/jpeg', this.exportQuality);
      const blob = this.dataUrlToBlob(dataUrl);
      observer.next(blob);
      observer.complete();
    });
  }
}
