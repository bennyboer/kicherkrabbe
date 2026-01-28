import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { OverlayService } from '../../services';
import { Observable } from 'rxjs';
import { Overlay } from '../../models';

@Component({
  selector: 'app-overlay-container',
  templateUrl: './overlay-container.component.html',
  styleUrls: ['./overlay-container.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class OverlayContainerComponent {
  @Input({ required: true })
  set parentElement(element: HTMLElement) {
    this.overlayService.setOverlayParentElement(element);
  }

  constructor(private readonly overlayService: OverlayService) {}

  getOverlays(): Observable<Overlay[]> {
    return this.overlayService.getOverlays();
  }
}
