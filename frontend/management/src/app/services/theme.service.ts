import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, distinctUntilChanged, Observable, Subject, takeUntil } from 'rxjs';

export enum Theme {
  LIGHT = 'LIGHT',
  DARK = 'DARK',
}

const THEME_CLASS_NAME_LOOKUP = {
  [Theme.LIGHT]: 'light',
  [Theme.DARK]: 'dark',
};

@Injectable()
export class ThemeService implements OnDestroy {
  private readonly theme$: BehaviorSubject<Theme> = new BehaviorSubject<Theme>(Theme.LIGHT);
  private readonly destroy$: Subject<void> = new Subject<void>();

  private darkModeMediaQuery!: MediaQueryList;
  private darkModeEventListener!: (event: MediaQueryListEvent) => void;

  constructor() {
    this.theme$.pipe(takeUntil(this.destroy$)).subscribe((theme) => this.applyTheme(theme));

    this.listenToSystemPreferences();
    this.applyTheme(this.theme$.value);
  }

  ngOnDestroy(): void {
    this.theme$.complete();

    this.destroy$.next();
    this.destroy$.complete();

    this.deregisterSystemPreferencesListener();
  }

  setTheme(theme: Theme): void {
    this.theme$.next(theme);
  }

  getTheme(): Observable<Theme> {
    return this.theme$.asObservable().pipe(distinctUntilChanged());
  }

  private applyTheme(theme: Theme): void {
    const body: HTMLElement = document.body;

    body.classList.remove(...Object.values(THEME_CLASS_NAME_LOOKUP));
    body.classList.add(THEME_CLASS_NAME_LOOKUP[theme]);
  }

  private listenToSystemPreferences(): void {
    this.darkModeMediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    this.darkModeEventListener = (event: MediaQueryListEvent) => {
      if (event.matches) {
        this.setTheme(Theme.DARK);
      } else {
        this.setTheme(Theme.LIGHT);
      }
    };
    this.darkModeMediaQuery.addEventListener('change', this.darkModeEventListener);

    if (this.darkModeMediaQuery.matches) {
      this.setTheme(Theme.DARK);
    } else {
      this.setTheme(Theme.LIGHT);
    }
  }

  private deregisterSystemPreferencesListener(): void {
    this.darkModeMediaQuery.removeEventListener('change', this.darkModeEventListener);
  }
}
