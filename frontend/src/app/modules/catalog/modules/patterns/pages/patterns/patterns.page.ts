import { ChangeDetectionStrategy, Component } from '@angular/core';
import { PatternsStoreService } from '../../services';
import { map } from 'rxjs';
import { CardListItem } from '../../../../../shared';
import { Pattern } from '../../model';

@Component({
  selector: 'app-patterns-page',
  templateUrl: './patterns.page.html',
  styleUrls: ['./patterns.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternsPage {
  protected readonly items$ = this.patternsStore
    .getPatterns()
    .pipe(
      map((patterns) =>
        patterns.map((pattern) => this.mapPatternToItem(pattern)),
      ),
    );

  constructor(private readonly patternsStore: PatternsStoreService) {}

  private mapPatternToItem(pattern: Pattern): CardListItem {
    return CardListItem.of({
      title: pattern.name,
      description: `ab ${pattern.getStartingPrice().formatted()}, Größe ${pattern.getSmallestSize()} - ${pattern.getLargestSize()}`,
      link: `/catalog/patterns/${pattern.id}`,
      imageUrl: pattern.images[0].url ?? '',
    });
  }
}
