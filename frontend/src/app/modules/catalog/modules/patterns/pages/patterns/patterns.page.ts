import { ChangeDetectionStrategy, Component } from '@angular/core';
import { PatternsStoreService } from '../../services';
import { map, Observable } from 'rxjs';
import { CardListItem } from '../../../../../shared';
import { Pattern } from '../../model';

@Component({
  selector: 'app-patterns-page',
  templateUrl: './patterns.page.html',
  styleUrls: ['./patterns.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternsPage {
  protected readonly items$: Observable<CardListItem[]> = this.patternsStore
    .getPatterns()
    .pipe(
      map((patterns) =>
        patterns
          .sort((a, b) =>
            a.name.localeCompare(b.name, 'de-de', {
              sensitivity: 'base',
              numeric: true,
            }),
          )
          .map((pattern) => this.mapPatternToItem(pattern)),
      ),
    );

  constructor(private readonly patternsStore: PatternsStoreService) {}

  private mapPatternToItem(pattern: Pattern): CardListItem {
    return CardListItem.of({
      title: pattern.name,
      description: `ab ${pattern.getStartingPrice().formatted()}, Größe ${pattern.getFormattedSizeRange()}`,
      link: `/catalog/patterns/${pattern.id}`,
      imageUrl: pattern.previewImage.url ?? '',
    });
  }
}
