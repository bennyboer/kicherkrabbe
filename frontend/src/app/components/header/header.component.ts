import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
} from '@angular/core';
import {
  animationFrameScheduler,
  BehaviorSubject,
  distinctUntilChanged,
  filter,
  fromEvent,
  Observable,
  Subject,
  takeUntil,
  throttleTime,
} from 'rxjs';
import { NavigationEnd, Router } from '@angular/router';
import { ButtonSize } from '../../modules/shared';
import { none, Option, some } from '../../modules/shared/modules/option';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeaderComponent implements AfterViewInit, OnDestroy {
  protected headerHeight: Option<number> = none();

  private readonly sticky$: Subject<boolean> = new BehaviorSubject(false);
  private readonly overlayActive$: Subject<boolean> = new BehaviorSubject(
    false,
  );
  private readonly destroy$: Subject<void> = new Subject<void>();

  protected readonly ButtonSize = ButtonSize;

  constructor(
    private readonly elementRef: ElementRef<HTMLElement>,
    private readonly router: Router,
  ) {
    this.router.events
      .pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntil(this.destroy$),
      )
      .subscribe(() => {
        this.closeOverlay();
      });
  }

  ngAfterViewInit(): void {
    this.headerHeight = some(this.elementRef.nativeElement.clientHeight);

    this.setupScrollListener();
  }

  ngOnDestroy(): void {
    this.sticky$.complete();
    this.overlayActive$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  isSticky(): Observable<boolean> {
    return this.sticky$.asObservable().pipe(distinctUntilChanged());
  }

  isOverlayActive(): Observable<boolean> {
    return this.overlayActive$.asObservable().pipe(distinctUntilChanged());
  }

  openOverlay(): void {
    this.overlayActive$.next(true);
  }

  closeOverlay(): void {
    this.overlayActive$.next(false);
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
