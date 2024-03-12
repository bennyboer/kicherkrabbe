import { ChangeDetectionStrategy, Component } from '@angular/core';
import { map, Observable } from 'rxjs';
import { CardListItem } from '../../../../../shared';
import { Fabric } from '../../model';
import { FabricsStoreService } from '../../services';

@Component({
  selector: 'app-fabrics-page',
  templateUrl: './fabrics.page.html',
  styleUrls: ['./fabrics.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FabricsPage {
  protected readonly items$: Observable<CardListItem[]> = this.fabricsStore
    .getFabrics()
    .pipe(
      map((patterns) =>
        patterns.map((pattern) => this.mapFabricToItem(pattern)),
      ),
    );

  constructor(private readonly fabricsStore: FabricsStoreService) {}

  private mapFabricToItem(fabric: Fabric): CardListItem {
    return CardListItem.of({
      title: fabric.name,
      description: fabric.getStockStatusLabel(),
      link: `/catalog/fabrics/${fabric.id}`,
      imageUrl: fabric.image.url ?? '',
    });
  }
}
