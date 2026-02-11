import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AsyncPipe, NgOptimizedImage } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay } from 'rxjs/operators';
import { HighlightsService, LinkType, PublishedHighlight } from './highlights.service';

interface DisplayHighlight {
  imageUrl: string;
  link: string | null;
  usesImageLoader: boolean;
}

const FALLBACK_IMAGES: string[] = [
  'images/highlights/left.jpg',
  'images/highlights/right-top.jpg',
  'images/highlights/right-bottom.jpg',
];

@Component({
  selector: 'app-highlights',
  templateUrl: './highlights.html',
  styleUrl: './highlights.scss',
  standalone: true,
  imports: [AsyncPipe, NgOptimizedImage, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HighlightsComponent {
  private readonly highlightsService = inject(HighlightsService);

  readonly highlights$: Observable<DisplayHighlight[]> = this.highlightsService.getPublished().pipe(
    catchError(() => of([])),
    map((highlights) => this.toDisplayHighlights(highlights)),
    shareReplay(1)
  );

  private toDisplayHighlights(highlights: PublishedHighlight[]): DisplayHighlight[] {
    const result: DisplayHighlight[] = [];

    for (let i = 0; i < 3; i++) {
      if (i < highlights.length) {
        result.push({
          imageUrl: highlights[i].imageId,
          link: this.getHighlightLink(highlights[i]),
          usesImageLoader: true,
        });
      } else {
        result.push({
          imageUrl: FALLBACK_IMAGES[i],
          link: null,
          usesImageLoader: false,
        });
      }
    }

    return result;
  }

  private getHighlightLink(highlight: PublishedHighlight): string | null {
    if (highlight.links.length === 0) {
      return null;
    }

    const firstLink = highlight.links[0];
    if (firstLink.type === LinkType.PATTERN) {
      return `/patterns/${firstLink.id}`;
    } else if (firstLink.type === LinkType.FABRIC) {
      return `/fabrics/${firstLink.id}`;
    }

    return null;
  }
}
