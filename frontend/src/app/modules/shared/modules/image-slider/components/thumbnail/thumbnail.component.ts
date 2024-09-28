import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnDestroy,
} from '@angular/core';
import { ImageSliderImage } from '../../models';
import { BehaviorSubject, ReplaySubject, Subject } from 'rxjs';
import { someOrNone } from '../../../option';

@Component({
  selector: 'app-image-slider-thumbnail',
  templateUrl: './thumbnail.component.html',
  styleUrls: ['./thumbnail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ThumbnailComponent implements OnDestroy {
  protected readonly image$: Subject<ImageSliderImage> =
    new ReplaySubject<ImageSliderImage>(1);
  protected readonly active$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);

  @Input({ required: true })
  set image(value: ImageSliderImage) {
    someOrNone(value).map((thumbnail) => this.image$.next(thumbnail));
  }

  @Input()
  set active(value: boolean) {
    someOrNone(value).map((active) => this.active$.next(active));
  }

  ngOnDestroy(): void {
    this.image$.complete();
    this.active$.complete();
  }
}
