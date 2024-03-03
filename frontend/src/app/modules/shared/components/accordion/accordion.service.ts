import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, map, Observable, ReplaySubject, Subject } from 'rxjs';

type ItemId = string;

@Injectable()
export class AccordionService implements OnDestroy {
  private readonly items$: BehaviorSubject<Set<ItemId>> = new BehaviorSubject<
    Set<ItemId>
  >(new Set());
  private readonly openedItem$: Subject<ItemId> = new ReplaySubject(1);

  ngOnDestroy(): void {
    this.items$.complete();
  }

  isOpened(id: ItemId): Observable<boolean> {
    return this.openedItem$.pipe(map((openedId) => openedId === id));
  }

  register(id: ItemId): void {
    const items = this.items$.value;
    const updatedItems = new Set([...items]);
    const isUpdated = updatedItems.add(id);
    if (isUpdated) {
      this.items$.next(updatedItems);

      if (updatedItems.size === 1) {
        this.openedItem$.next(id);
      }
    }
  }

  unregister(id: ItemId): void {
    const items = this.items$.value;
    const updatedItems = new Set([...items]);
    const isUpdated = updatedItems.delete(id);
    if (isUpdated) {
      this.items$.next(updatedItems);
    }
  }

  open(id: string): void {
    this.openedItem$.next(id);
  }
}
