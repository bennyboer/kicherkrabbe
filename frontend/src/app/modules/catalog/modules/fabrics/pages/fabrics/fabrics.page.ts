import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { combineLatest, map, ReplaySubject, Subject, takeUntil } from 'rxjs';
import { CardListItem, SortProperty } from '../../../../../shared';
import { Fabric } from '../../model';
import { FabricsStoreService } from '../../services';
import { SortedEvent } from '../../../../../shared/components/sort-selector/sort-selector.component';

@Component({
  selector: 'app-fabrics-page',
  templateUrl: './fabrics.page.html',
  styleUrls: ['./fabrics.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FabricsPage implements OnInit, OnDestroy {
  private readonly sortOrder$: Subject<SortedEvent> =
    new ReplaySubject<SortedEvent>(1);
  private readonly destroy$: Subject<void> = new Subject<void>();
  protected readonly items$: Subject<CardListItem[]> = new ReplaySubject<
    CardListItem[]
  >(1);

  protected readonly sortProperties: SortProperty[] = [
    SortProperty.of({ id: 'name', label: 'Name' }),
    SortProperty.of({ id: 'availability', label: 'Verfügbarkeit' }),
  ];

  constructor(private readonly fabricsStore: FabricsStoreService) {}

  ngOnDestroy(): void {
    this.sortOrder$.complete();
    this.items$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnInit(): void {
    combineLatest([this.fabricsStore.getFabrics(), this.sortOrder$])
      .pipe(
        map(([fabrics, order]) =>
          this.sortItems(fabrics, order).map((fabric) =>
            this.mapFabricToItem(fabric),
          ),
        ),
        takeUntil(this.destroy$),
      )
      .subscribe((items) => this.items$.next(items));
  }

  protected onSort(event: SortedEvent): void {
    this.sortOrder$.next(event);
  }

  private mapFabricToItem(fabric: Fabric): CardListItem {
    return CardListItem.of({
      title: fabric.name,
      description: fabric.getStockStatusLabel(),
      link: `/catalog/fabrics/${fabric.id}`,
      imageUrl: fabric.image.url ?? '',
    });
  }

  private sortItems(fabrics: Fabric[], order: SortedEvent): Fabric[] {
    const result = [...fabrics].sort((a, b) => {
      const property = order.property;
      if (property.id === 'availability') {
        const v = a.availability.isAvailableInAnyType() ? 1 : 0;
        if (v === 0) {
          return this.compareByName(a, b);
        }
      } else if (property.id === 'name') {
        return this.compareByName(a, b);
      } else {
        throw new Error(`Unknown sort property: ${property.id}`);
      }
    });

    if (!order.ascending) {
      result.reverse();
    }

    return result;
  }

  private compareByName(a: Fabric, b: Fabric): number {
    return a.name.localeCompare(b.name, 'de-de', {
      sensitivity: 'base',
      numeric: true,
    });
  }
}
