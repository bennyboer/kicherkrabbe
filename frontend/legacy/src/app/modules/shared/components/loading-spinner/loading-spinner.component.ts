import {
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

@Component({
    selector: 'app-loading-spinner',
    templateUrl: './loading-spinner.component.html',
    styleUrls: ['./loading-spinner.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class LoadingSpinnerComponent implements OnInit, OnDestroy {
  private readonly size$: BehaviorSubject<number> = new BehaviorSubject<number>(32);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly renderer: Renderer2,
    private readonly elementRef: ElementRef,
  ) {}

  @Input({ transform: numberAttribute })
  set size(size: number) {
    this.size$.next(size);
  }

  ngOnInit(): void {
    this.size$.pipe(takeUntil(this.destroy$)).subscribe((size) => {
      this.renderer.setStyle(this.elementRef.nativeElement, '--size', `${size}px`, RendererStyleFlags2.DashCase);
    });
  }

  ngOnDestroy(): void {
    this.size$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }
}
