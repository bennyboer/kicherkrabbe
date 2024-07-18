import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PatternsStoreService } from '../../services';
import { map, Observable, switchMap } from 'rxjs';
import { Image } from '../../../../../../util';
import { ImageSliderImage } from '../../../../../shared/modules/image-slider';
import { Theme, ThemeService } from '../../../../../../services';

@Component({
  selector: 'app-pattern-page',
  templateUrl: './pattern.page.html',
  styleUrls: ['./pattern.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternPage {
  protected readonly pattern$ = this.route.params.pipe(
    map((params) => params['id']),
    switchMap((id) => this.patternsStore.getPatternById(id)),
  );
  protected readonly images$: Observable<ImageSliderImage[]> =
    this.pattern$.pipe(
      map((pattern) => this.toImageSliderImages(pattern.images)),
    );
  protected readonly theme$ = this.themeService
    .getTheme()
    .pipe(map((theme) => (theme === Theme.DARK ? 'dark' : 'light')));

  constructor(
    private readonly patternsStore: PatternsStoreService,
    private readonly route: ActivatedRoute,
    private readonly themeService: ThemeService,
  ) {}

  toImageSliderImages(images: Image[]): ImageSliderImage[] {
    return images.map((image) => ImageSliderImage.of({ url: image.url }));
  }
}
