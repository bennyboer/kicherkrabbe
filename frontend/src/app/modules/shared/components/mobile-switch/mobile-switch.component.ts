import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  BehaviorSubject,
  distinctUntilChanged,
  map,
  Observable,
  Subject,
  takeUntil,
} from 'rxjs';
import { BreakpointObserver } from '@angular/cdk/layout';

@Component({
  selector: 'app-mobile-switch',
  templateUrl: './mobile-switch.component.html',
  styleUrls: ['./mobile-switch.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MobileSwitchComponent implements OnInit, OnDestroy {
  private readonly isMobile$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(private readonly breakpointObserver: BreakpointObserver) {}

  ngOnInit(): void {
    this.breakpointObserver
      .observe('(max-width: 1000px)')
      .pipe(
        map((breakpoints) => breakpoints.matches),
        distinctUntilChanged(),
        takeUntil(this.destroy$),
      )
      .subscribe((isMobile) => this.isMobile$.next(isMobile));
  }

  ngOnDestroy() {
    this.isMobile$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  isMobile(): Observable<boolean> {
    return this.isMobile$.asObservable();
  }
}
