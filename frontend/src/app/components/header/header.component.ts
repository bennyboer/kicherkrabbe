import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  Input,
  OnDestroy,
} from '@angular/core';
import {
  animationFrameScheduler,
  BehaviorSubject,
  fromEvent,
  map,
  Observable,
  Subject,
  takeUntil,
  throttleTime,
} from 'rxjs';
import { Option } from '../../util';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeaderComponent implements AfterViewInit, OnDestroy {
  @Input()
  startHeight: number = 128;

  @Input()
  endHeight: number = 64;

  @Input()
  scrollDistance: number = 100;

  @Input()
  scrollContainerSelector: string = 'body';

  private readonly progress$: Subject<number> = new BehaviorSubject(0.0);

  private readonly destroy$: Subject<void> = new Subject<void>();

  ngAfterViewInit(): void {
    this.setupScrollListener();
  }

  ngOnDestroy(): void {
    this.progress$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getHeight(): Observable<number> {
    return this.progress$.asObservable().pipe(
      map((progress) => {
        const heightDiff = this.endHeight - this.startHeight;
        const heightProgress = progress * heightDiff;

        return this.startHeight + heightProgress;
      }),
    );
  }

  private setupScrollListener(): void {
    const scrollContainer = Option.someOrNone(
      document.querySelector(this.scrollContainerSelector),
    ).orElseThrow(
      `Could not resolve scroll container for selector '${this.scrollContainerSelector}'`,
    );

    fromEvent(scrollContainer!, 'scroll', { passive: true })
      .pipe(throttleTime(0, animationFrameScheduler), takeUntil(this.destroy$))
      .subscribe(() => {
        this.onScroll(scrollContainer as HTMLElement);
      });
  }

  private onScroll(container: HTMLElement): void {
    this.updateScrollProgress(container);
  }

  private updateScrollProgress(container: HTMLElement): void {
    const scrollHeight = container.scrollHeight - container.clientHeight;
    const scrollTop = container.scrollTop;
    const progress = scrollTop / scrollHeight;

    this.progress$.next(progress);
  }
}
