import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { Theme, ThemeService } from '../../services';
import { BehaviorSubject, map, Observable } from 'rxjs';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FooterComponent implements OnDestroy {
  private readonly showHiddenThingsCounter$: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  protected readonly showHiddenThings$: Observable<boolean> = this.showHiddenThingsCounter$.pipe(
    map((counter) => counter > 10),
  );

  constructor(private readonly themeService: ThemeService) {}

  ngOnDestroy(): void {
    this.showHiddenThingsCounter$.complete();
  }

  isDarkMode(): Observable<boolean> {
    return this.themeService.getTheme().pipe(map((theme) => theme === Theme.DARK));
  }

  isLightMode(): Observable<boolean> {
    return this.themeService.getTheme().pipe(map((theme) => theme === Theme.LIGHT));
  }

  incrementShowHiddenThingsCounter(): void {
    this.showHiddenThingsCounter$.next(this.showHiddenThingsCounter$.value + 1);
  }
}
