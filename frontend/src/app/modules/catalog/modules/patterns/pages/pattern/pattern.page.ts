import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { map, Observable, switchMap } from 'rxjs';
import { ImageSliderImage } from '../../../../../shared/modules/image-slider';
import { Theme, ThemeService } from '../../../../../../services';
import { PatternsService } from '../../services';
import { environment } from '../../../../../../../environments';
import { ImageId } from '../../model';

@Component({
    selector: 'app-pattern-page',
    templateUrl: './pattern.page.html',
    styleUrls: ['./pattern.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class PatternPage {
  protected readonly pattern$ = this.route.params.pipe(
    map((params) => params['id']),
    switchMap((id) => this.patternsService.getPattern(id)),
  );
  protected readonly images$: Observable<ImageSliderImage[]> = this.pattern$.pipe(
    map((pattern) => this.toImageSliderImages(pattern.images)),
  );
  protected readonly theme$ = this.themeService
    .getTheme()
    .pipe(map((theme) => (theme === Theme.DARK ? 'dark' : 'light')));

  constructor(
    private readonly patternsService: PatternsService,
    private readonly route: ActivatedRoute,
    private readonly themeService: ThemeService,
  ) {}

  toImageSliderImages(images: ImageId[]): ImageSliderImage[] {
    return images.map((imageId) => ImageSliderImage.of({ url: this.getImageUrl(imageId) }));
  }

  private getImageUrl(imageId: ImageId): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }
}
