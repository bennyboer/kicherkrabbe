import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  HostBinding,
  HostListener,
  Input,
} from '@angular/core';
import { Overlay } from '../../models';

@Component({
  selector: 'app-overlay',
  templateUrl: './overlay.component.html',
  styleUrls: ['./overlay.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class OverlayComponent {
  @Input({ required: true })
  overlay!: Overlay;

  constructor(private readonly elementRef: ElementRef) {}

  @HostBinding('style.z-index')
  get zIndex(): number {
    return 1000 + this.overlay.index;
  }

  @HostBinding('style.left.px')
  get left(): number {
    return this.overlay.offset.x;
  }

  @HostBinding('style.top.px')
  get top(): number {
    return this.overlay.offset.y;
  }

  @HostBinding('style.min-width.px')
  get minWidth(): number {
    return this.overlay.minWidth;
  }

  @HostListener('document:click', ['$event'])
  onClickOut(event: MouseEvent) {
    if (!event.target) {
      return;
    }

    const target = event.target as Node;
    const isClickOnParent = this.overlay.parent.contains(target);
    const isClickOnOverlay = this.elementRef.nativeElement.contains(target);

    const isClickOnBackdrop = !isClickOnOverlay && !isClickOnParent;

    if (isClickOnBackdrop) {
      this.overlay.close();
    }
  }
}
