import { Injectable, OnDestroy, TemplateRef } from '@angular/core';
import {
  BehaviorSubject,
  filter,
  map,
  Observable,
  Subject,
  takeUntil,
} from 'rxjs';
import { Point } from '../../../util';
import { Overlay, OverlayId } from '../models';
import { Option, someOrNone } from '../modules/option';

interface OverlayLookup {
  [key: string]: Overlay;
}

export class OverlayRef {
  readonly id: OverlayId;
  private readonly overlay: Overlay;

  private constructor(overlay: Overlay) {
    this.id = overlay.id;
    this.overlay = overlay;
  }

  static of(overlay: Overlay): OverlayRef {
    return new OverlayRef(overlay);
  }

  close(): void {
    this.overlay.close();
  }

  closed(): Observable<boolean> {
    return this.overlay.isClosed();
  }

  isOpened(): Observable<boolean> {
    return this.overlay.isOpened();
  }

  isCurrentlyOpened(): boolean {
    return this.overlay.isCurrentlyOpened();
  }
}

@Injectable()
export class OverlayService implements OnDestroy {
  private overlayParentElement: HTMLElement = window.document.body;
  private readonly lookup$: BehaviorSubject<OverlayLookup> =
    new BehaviorSubject<OverlayLookup>({});
  private readonly destroy$: Subject<void> = new Subject<void>();

  ngOnDestroy(): void {
    this.lookup$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  pushOverlay(props: {
    templateRef: TemplateRef<any>;
    parent: HTMLElement;
    offset: Point;
    closeOnBackdropClick?: boolean;
    minWidth?: number;
  }): OverlayRef {
    const { templateRef, offset, parent } = props;
    const closeOnBackdropClick = someOrNone(props.closeOnBackdropClick).orElse(
      true,
    );
    const minWidth = someOrNone(props.minWidth).orElse(0);

    const index = this.getNextOverlayIndex();
    const overlay = Overlay.create({
      index,
      templateRef,
      parent,
      offset: this.normalizeOffset(parent, offset),
      closeOnBackdropClick,
      minWidth,
    });

    this.updateLookup((lookup) => {
      lookup[overlay.id] = overlay;
      return true;
    });

    overlay
      .isClosed()
      .pipe(
        filter((closed) => closed),
        takeUntil(this.destroy$),
      )
      .subscribe(() => {
        this.removeOverlay(overlay.id);
      });

    return OverlayRef.of(overlay);
  }

  popOverlay(): void {
    const indexToPop = this.getMaxOverlayIndex();
    this.removeOverlayByIndex(indexToPop);
  }

  removeOverlayByIndex(index: number): void {
    this.getOverlayByIndex(index).ifSome((overlay) =>
      this.removeOverlay(overlay.id),
    );
  }

  removeOverlay(id: OverlayId): void {
    this.getOverlayById(id).ifSome((overlay) => {
      this.updateLookup((lookup) => {
        delete lookup[id];
        return true;
      });
    });
  }

  getOverlays(): Observable<Overlay[]> {
    return this.lookup$
      .asObservable()
      .pipe(
        map((lookup) =>
          Object.values(lookup).sort((a, b) => a.index - b.index),
        ),
      );
  }

  setOverlayParentElement(element: HTMLElement): void {
    this.overlayParentElement = element;
  }

  private getCurrentOverlayLookup(): OverlayLookup {
    return this.lookup$.value;
  }

  private getNextOverlayIndex(): number {
    return this.getOverlayCount();
  }

  private getMaxOverlayIndex(): number {
    return this.getOverlayCount() - 1;
  }

  private getOverlayCount(): number {
    return Object.keys(this.getCurrentOverlayLookup()).length;
  }

  private getOverlayById(id: OverlayId): Option<Overlay> {
    return someOrNone(this.getCurrentOverlayLookup()[id]);
  }

  private getOverlayByIndex(index: number): Option<Overlay> {
    return someOrNone(
      Object.values(this.getCurrentOverlayLookup()).find(
        (o) => o.index === index,
      ),
    );
  }

  private updateLookup(consumer: (lookup: OverlayLookup) => boolean): void {
    const currentLookup = this.getCurrentOverlayLookup();
    const updatedLookup = { ...currentLookup };
    const updated = consumer(updatedLookup);
    if (updated) {
      this.lookup$.next(updatedLookup);
    }
  }

  private normalizeOffset(parent: HTMLElement, offset: Point): Point {
    const overlayParent = this.overlayParentElement;
    const overlayParentRect = overlayParent.getBoundingClientRect();
    const parentRect = parent.getBoundingClientRect();

    const x = offset.x + parentRect.left - overlayParentRect.left;
    const y = offset.y + parentRect.top - overlayParentRect.top;

    return Point.of({ x, y });
  }
}
