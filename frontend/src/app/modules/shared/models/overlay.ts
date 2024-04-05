import { TemplateRef } from '@angular/core';
import { Point } from '../../../util';
import { BehaviorSubject, map, Observable } from 'rxjs';

export type OverlayId = string;

export class Overlay {
  readonly id: OverlayId;
  readonly index: number;
  readonly templateRef: TemplateRef<any>;
  readonly parent: HTMLElement;
  readonly offset: Point;
  readonly closeOnBackdropClick: boolean;
  readonly minWidth: number;
  private readonly closed$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);

  private constructor(props: {
    id: OverlayId;
    index: number;
    templateRef: TemplateRef<any>;
    parent: HTMLElement;
    offset: Point;
    closeOnBackdropClick: boolean;
    minWidth: number;
  }) {
    this.id = props.id;
    this.index = props.index;
    this.templateRef = props.templateRef;
    this.offset = props.offset;
    this.closeOnBackdropClick = props.closeOnBackdropClick;
    this.minWidth = props.minWidth;
    this.parent = props.parent;
  }

  static create(props: {
    index: number;
    templateRef: TemplateRef<any>;
    parent: HTMLElement;
    offset: Point;
    closeOnBackdropClick: boolean;
    minWidth: number;
  }): Overlay {
    const id = crypto.randomUUID();

    return new Overlay({
      id,
      index: props.index,
      templateRef: props.templateRef,
      offset: props.offset,
      closeOnBackdropClick: props.closeOnBackdropClick,
      minWidth: props.minWidth,
      parent: props.parent,
    });
  }

  close(): void {
    this.closed$.next(true);
    this.closed$.complete();
  }

  isClosed(): Observable<boolean> {
    return this.closed$.asObservable();
  }

  isOpened(): Observable<boolean> {
    return this.isClosed().pipe(map((isClosed) => !isClosed));
  }

  isCurrentlyOpened(): boolean {
    return !this.closed$.value;
  }
}
