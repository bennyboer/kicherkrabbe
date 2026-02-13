import { ChangeDetectionStrategy, Component, inject, signal, viewChild } from '@angular/core';
import { AsyncPipe, NgOptimizedImage } from '@angular/common';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay } from 'rxjs/operators';
import { Popover } from 'primeng/popover';
import { HighlightsService, Link, LinkType, PublishedHighlight } from './highlights.service';

interface DisplayLink {
  name: string;
  route: string;
}

interface DisplayHighlight {
  imageUrl: string;
  links: DisplayLink[];
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
  imports: [AsyncPipe, NgOptimizedImage, Popover],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HighlightsComponent {
  private readonly highlightsService = inject(HighlightsService);
  private readonly router = inject(Router);

  private readonly popover = viewChild.required<Popover>('popover');

  readonly highlights$: Observable<DisplayHighlight[]> = this.highlightsService.getPublished().pipe(
    catchError(() => of([])),
    map((highlights) => this.toDisplayHighlights(highlights)),
    shareReplay(1)
  );

  readonly popoverLinks = signal<DisplayLink[]>([]);

  onHighlightClick(event: MouseEvent, highlight: DisplayHighlight): void {
    if (highlight.links.length === 0) {
      return;
    }

    if (highlight.links.length === 1) {
      this.router.navigate([highlight.links[0].route]);
      return;
    }

    this.popoverLinks.set(highlight.links);

    const popover = this.popover();
    popover.show(event);

    setTimeout(() => {
      const popoverEl = document.querySelector('.p-popover') as HTMLElement;
      if (popoverEl) {
        const arrowOffset = 20;
        popoverEl.style.left = `${event.pageX - arrowOffset}px`;
        popoverEl.style.top = `${event.pageY}px`;
      }
    });
  }

  onLinkClick(link: DisplayLink): void {
    this.popover().hide();
    this.router.navigate([link.route]);
  }

  private toDisplayHighlights(highlights: PublishedHighlight[]): DisplayHighlight[] {
    const result: DisplayHighlight[] = [];

    for (let i = 0; i < 3; i++) {
      if (i < highlights.length) {
        result.push({
          imageUrl: highlights[i].imageId,
          links: this.toDisplayLinks(highlights[i].links),
          usesImageLoader: true,
        });
      } else {
        result.push({
          imageUrl: FALLBACK_IMAGES[i],
          links: [],
          usesImageLoader: false,
        });
      }
    }

    return result;
  }

  private toDisplayLinks(links: Link[]): DisplayLink[] {
    return links
      .map((link) => {
        const route = this.getLinkRoute(link);
        if (!route) {
          return null;
        }
        return { name: link.name, route };
      })
      .filter((link): link is DisplayLink => link !== null);
  }

  private getLinkRoute(link: Link): string | null {
    if (link.type === LinkType.PATTERN) {
      return `/patterns/${link.id}`;
    } else if (link.type === LinkType.FABRIC) {
      return `/fabrics/${link.id}`;
    }
    return null;
  }
}
