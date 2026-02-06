import { AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, OnDestroy } from '@angular/core';
import {
  animationFrameScheduler,
  BehaviorSubject,
  distinctUntilChanged,
  fromEvent,
  Observable,
  Subject,
  takeUntil,
  throttleTime,
} from 'rxjs';
import { none, Option, some } from '@kicherkrabbe/shared';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class HeaderComponent implements AfterViewInit, OnDestroy {
  protected headerHeight: Option<number> = none();

  private readonly sticky$: Subject<boolean> = new BehaviorSubject(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  private resizeObserver: Option<ResizeObserver> = none();

  constructor(private readonly elementRef: ElementRef<HTMLElement>) {}

  ngAfterViewInit(): void {
    const resizeObserver = new ResizeObserver(() => {
      this.headerHeight = some(this.elementRef.nativeElement.clientHeight);
    });
    resizeObserver.observe(this.elementRef.nativeElement);
    this.resizeObserver = some(resizeObserver);

    this.setupScrollListener();
  }

  ngOnDestroy(): void {
    this.resizeObserver.ifSome((ro) => ro.disconnect());

    this.sticky$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  isSticky(): Observable<boolean> {
    return this.sticky$.asObservable().pipe(distinctUntilChanged());
  }

  private setupScrollListener(): void {
    fromEvent(window, 'scroll', { passive: true })
      .pipe(throttleTime(0, animationFrameScheduler), takeUntil(this.destroy$))
      .subscribe(() => this.onScroll());
  }

  private onScroll(): void {
    this.updateScrollProgress();
  }

  private updateScrollProgress(): void {
    const scrollDistance = window.scrollY;
    const isSticky = scrollDistance > this.headerHeight.orElse(256);

    this.sticky$.next(isSticky);
  }
}
