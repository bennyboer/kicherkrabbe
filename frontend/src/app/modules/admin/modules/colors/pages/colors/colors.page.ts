import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable } from 'rxjs';
import { ColorsService } from '../../services';
import { Color } from '../../model';

@Component({
  selector: 'app-colors-page',
  templateUrl: './colors.page.html',
  styleUrls: ['./colors.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ColorsPage implements OnDestroy {
  private readonly search$: BehaviorSubject<string> = new BehaviorSubject<string>('');

  constructor(private readonly colorsService: ColorsService) {}

  ngOnDestroy(): void {
    this.search$.complete();
  }

  getColors(): Observable<Color[]> {
    return combineLatest([this.colorsService.getColors(), this.search$]).pipe(
      map(([colors, search]) => colors.filter((color) => color.name.toLowerCase().includes(search.toLowerCase()))),
    );
  }

  isSearching(): Observable<boolean> {
    return this.search$.pipe(map((search) => search.length > 0));
  }

  isLoading(): Observable<boolean> {
    return this.colorsService.isLoading();
  }

  isFailed(): Observable<boolean> {
    return this.colorsService.isFailedLoadingColors();
  }

  updateSearch(value: string): void {
    this.search$.next(value.trim());
  }
}
