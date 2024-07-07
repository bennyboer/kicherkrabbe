import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnDestroy,
} from '@angular/core';
import { Thumbnail } from '../../models';
import { ReplaySubject, Subject } from 'rxjs';
import { someOrNone } from '../../../../../../util';

@Component({
  selector: 'app-image-slider-thumbnail',
  templateUrl: './thumbnail.component.html',
  styleUrls: ['./thumbnail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ThumbnailComponent implements OnDestroy {
  protected readonly thumbnail$: Subject<Thumbnail> =
    new ReplaySubject<Thumbnail>(1);

  @Input({ required: true })
  set thumbnail(value: Thumbnail) {
    someOrNone(value).map((thumbnail) => this.thumbnail$.next(thumbnail));
  }

  ngOnDestroy(): void {
    this.thumbnail$.complete();
  }
}
