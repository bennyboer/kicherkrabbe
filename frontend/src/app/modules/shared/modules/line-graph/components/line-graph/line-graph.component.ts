import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  ViewChild,
} from '@angular/core';
import { none, Option, some, someOrNone } from '../../../option';
import { BehaviorSubject, combineLatest, map, Subject, takeUntil } from 'rxjs';
import { Theme, ThemeService } from '../../../../../../services';

@Component({
  selector: 'app-line-graph',
  templateUrl: './line-graph.component.html',
  styleUrls: ['./line-graph.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LineGraphComponent implements AfterViewInit, OnDestroy {
  @Input()
  set data(data: number[] | null) {
    someOrNone(data).ifSome((d) => this.data$.next(d));
  }

  @Input()
  set xLabels(labels: string[] | null) {
    someOrNone(labels).ifSome((l) => this.xLabels$.next(l));
  }

  @Input()
  set yLabels(labels: string[] | null) {
    someOrNone(labels).ifSome((l) => this.yLabels$.next(l));
  }

  @Input()
  set maxVisibleYLabels(maxVisibleYLabels: number | null) {
    someOrNone(maxVisibleYLabels).ifSome((m) =>
      this.maxVisibleYLabels$.next(m),
    );
  }

  @Input()
  set maxVisibleXLabels(maxVisibleXLabels: number | null) {
    someOrNone(maxVisibleXLabels).ifSome((m) =>
      this.maxVisibleXLabels$.next(m),
    );
  }

  @ViewChild('canvas')
  canvas!: ElementRef<HTMLCanvasElement>;

  @ViewChild('container')
  container!: ElementRef<HTMLDivElement>;

  private resizeObserver: Option<ResizeObserver> = none();

  protected readonly xLabels$: BehaviorSubject<string[]> = new BehaviorSubject<
    string[]
  >([]);
  protected readonly yLabels$: BehaviorSubject<string[]> = new BehaviorSubject<
    string[]
  >([]);
  protected readonly data$: BehaviorSubject<number[]> = new BehaviorSubject<
    number[]
  >([]);
  protected readonly maxVisibleYLabels$: BehaviorSubject<number> =
    new BehaviorSubject<number>(99999999);
  protected readonly maxVisibleXLabels$: BehaviorSubject<number> =
    new BehaviorSubject<number>(99999999);

  private readonly destroy$: Subject<void> = new Subject<void>();

  private isDarkMode: boolean = false;

  constructor(private readonly themeService: ThemeService) {}

  ngAfterViewInit(): void {
    this.resizeCanvas();
    this.registerResizeObserver();

    this.themeService
      .getTheme()
      .pipe(
        map((theme) => theme === Theme.DARK),
        takeUntil(this.destroy$),
      )
      .subscribe((isDarkMode) => {
        if (this.isDarkMode !== isDarkMode) {
          this.isDarkMode = isDarkMode;
          this.repaint();
        }
      });

    combineLatest([
      this.xLabels$,
      this.yLabels$,
      this.data$,
      this.maxVisibleYLabels$,
      this.maxVisibleXLabels$,
    ])
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.repaint());
  }

  ngOnDestroy(): void {
    this.resizeObserver.ifSome((observer) => observer.disconnect());
    this.resizeObserver = none();

    this.xLabels$.complete();
    this.yLabels$.complete();
    this.data$.complete();
    this.maxVisibleYLabels$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  resizeCanvas(): void {
    if (!this.canvas || !this.container) {
      return;
    }

    const containerRect = this.container.nativeElement.getBoundingClientRect();
    const devicePixelRatio = window.devicePixelRatio || 1;

    const actualWidth = containerRect.width * devicePixelRatio;
    const actualHeight = containerRect.height * devicePixelRatio;
    const visibleWidth = containerRect.width;
    const visibleHeight = containerRect.height;

    const canvas = this.canvas.nativeElement;
    canvas.height = actualHeight;
    canvas.width = actualWidth;

    canvas.style.height = `${visibleHeight}px`;
    canvas.style.width = `${visibleWidth}px`;

    this.repaint();
  }

  registerResizeObserver(): void {
    const resizeObserver = new ResizeObserver(() => this.resizeCanvas());
    resizeObserver.observe(this.container.nativeElement);
    this.resizeObserver = some(resizeObserver);
  }

  repaint(): void {
    if (!this.canvas) {
      return;
    }

    const canvas = this.canvas.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) {
      return;
    }

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const longestYLabel = this.xLabels$.value.reduce((a, b) =>
      a.length > b.length ? a : b,
    );
    const longestYLabelWidth = ctx.measureText(longestYLabel).width;

    const xLabelsResult = this.repaintXLabels(ctx, longestYLabelWidth);
    const yLabelsResult = this.repaintYLabels(ctx, xLabelsResult.height);
    this.repaintGrid(
      ctx,
      xLabelsResult.tickPositions,
      yLabelsResult.tickPositions,
      xLabelsResult.height,
      yLabelsResult.width,
    );
    this.repaintData(
      ctx,
      xLabelsResult.tickPositions,
      yLabelsResult.tickPositions,
    );
  }

  private repaintXLabels(
    ctx: CanvasRenderingContext2D,
    yLabelWidth: number,
  ): {
    height: number;
    tickPositions: { pos: number; visible: boolean }[];
  } {
    const labels = this.xLabels$.value;
    let maxVisibleXLabels = this.maxVisibleXLabels$.value;
    if (maxVisibleXLabels < 2) {
      maxVisibleXLabels = 2;
    }

    let visibleLabels = labels.map((l) => ({ label: l, visible: true }));
    if (labels.length > maxVisibleXLabels) {
      visibleLabels = [{ label: labels[0], visible: true }];
      if (maxVisibleXLabels > 2) {
        const labelsToAdd = labels.length - 2;
        let step = Math.round(labelsToAdd / (maxVisibleXLabels - 1));

        for (let i = 1; i < labels.length - 1; i++) {
          const visible = i % step === 0;
          visibleLabels.push({ label: labels[i], visible });
        }
      }
      visibleLabels.push({ label: labels[labels.length - 1], visible: true });
    }

    ctx.font = `${11 * window.devicePixelRatio}px Lexend`;
    ctx.fillStyle = this.isDarkMode ? 'white' : 'black';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    const longestLabel = visibleLabels
      .filter((l) => l.visible)
      .reduce((a, b) => (a.label.length > b.label.length ? a : b));
    const longestLabelWidth = ctx.measureText(longestLabel.label).width;
    const xLabelsHeight = longestLabelWidth + 1;
    const xOffset = yLabelWidth + 10;
    const yOffset = this.canvas.nativeElement.height - xLabelsHeight;

    const availableSpace = this.canvas.nativeElement.width - xOffset;
    const labelCount = visibleLabels.length;

    const labelOffset = availableSpace / labelCount;

    const tickPositions = [];
    for (let i = 0; i < visibleLabels.length; i++) {
      const label = visibleLabels[i];

      const x = xOffset + labelOffset * i;
      const y = yOffset + xLabelsHeight / 2;

      if (label.visible) {
        ctx.save();
        ctx.translate(x, y);
        ctx.rotate(-Math.PI / 4);
        ctx.fillText(label.label, 0, 0);
        ctx.restore();
      }

      tickPositions.push({ pos: x, visible: label.visible });
    }

    return { height: xLabelsHeight, tickPositions };
  }

  private repaintYLabels(
    ctx: CanvasRenderingContext2D,
    xLabelsHeight: number,
  ): { width: number; tickPositions: { pos: number; visible: boolean }[] } {
    const labels = this.yLabels$.value;
    let maxVisibleYLabels = this.maxVisibleYLabels$.value;
    if (maxVisibleYLabels < 2) {
      maxVisibleYLabels = 2;
    }

    let visibleLabels = labels.map((l) => ({ label: l, visible: true }));
    if (labels.length > maxVisibleYLabels) {
      visibleLabels = [{ label: labels[0], visible: true }];
      if (maxVisibleYLabels > 2) {
        const labelsToAdd = labels.length - 2;
        let step = Math.round(labelsToAdd / (maxVisibleYLabels - 1));

        for (let i = 1; i < labels.length - 1; i++) {
          const visible = i % step === 0;
          visibleLabels.push({ label: labels[i], visible });
        }
      }
      visibleLabels.push({ label: labels[labels.length - 1], visible: true });
    }

    const yOffset = 20;
    const availableSpace =
      this.canvas.nativeElement.height - xLabelsHeight - yOffset;
    const labelCount = visibleLabels.length;

    const labelOffset = availableSpace / labelCount;

    ctx.font = `${12 * window.devicePixelRatio}px Lexend`;
    ctx.fillStyle = this.isDarkMode ? 'white' : 'black';
    ctx.textAlign = 'right';
    ctx.textBaseline = 'middle';

    const longestLabel = visibleLabels
      .filter((l) => l.visible)
      .reduce((a, b) => (a.label.length > b.label.length ? a : b));
    const longestLabelWidth = ctx.measureText(longestLabel.label).width;
    const yLabelsWidth = longestLabelWidth + 1;

    const tickPositions = [];
    for (let i = visibleLabels.length - 1; i >= 0; i--) {
      const label = visibleLabels[i];
      const reverseIndex = visibleLabels.length - 1 - i;

      const x = yLabelsWidth;
      const y = labelOffset * reverseIndex + yOffset;

      if (label.visible) {
        ctx.fillText(label.label, x, y);
      }

      tickPositions.push({ pos: y, visible: label.visible });
    }

    return { width: yLabelsWidth, tickPositions };
  }

  private repaintGrid(
    ctx: CanvasRenderingContext2D,
    xTicks: { pos: number; visible: boolean }[],
    yTicks: { pos: number; visible: boolean }[],
    xLabelsHeight: number,
    yLabelsWidth: number,
  ) {
    ctx.strokeStyle = this.isDarkMode ? '#333' : '#CCC';
    ctx.lineWidth = 1;

    for (let x = 0; x < xTicks.length; x++) {
      const tick = xTicks[x];

      if (!tick.visible) {
        continue;
      }

      ctx.beginPath();
      ctx.moveTo(tick.pos, 0);
      ctx.lineTo(
        tick.pos,
        this.canvas.nativeElement.height - xLabelsHeight - 5,
      );
      ctx.stroke();
    }

    for (let y = 0; y < yTicks.length; y++) {
      const tick = yTicks[y];

      if (!tick.visible) {
        continue;
      }

      ctx.beginPath();
      ctx.moveTo(yLabelsWidth + 5, tick.pos);
      ctx.lineTo(this.canvas.nativeElement.width, tick.pos);
      ctx.stroke();
    }
  }

  private repaintData(
    ctx: CanvasRenderingContext2D,
    xTicks: { pos: number; visible: boolean }[],
    yTicks: { pos: number; visible: boolean }[],
  ): void {
    const data = this.data$.value;
    if (data.length === 0) {
      return;
    }

    ctx.strokeStyle = '#66CC99';
    ctx.fillStyle = '#66CC99';
    ctx.lineWidth = 2;

    for (let i = 0; i < data.length; i++) {
      const d = data[i];

      const x = xTicks[i].pos;
      const y = yTicks[yTicks.length - 1 - d].pos;

      if (i === 0) {
        ctx.beginPath();
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    }

    ctx.stroke();

    for (let i = 0; i < data.length; i++) {
      const d = data[i];

      const x = xTicks[i].pos;
      const y = yTicks[yTicks.length - 1 - d].pos;

      ctx.beginPath();
      ctx.arc(x, y, 4, 0, Math.PI * 2);
      ctx.fill();
    }
  }
}
