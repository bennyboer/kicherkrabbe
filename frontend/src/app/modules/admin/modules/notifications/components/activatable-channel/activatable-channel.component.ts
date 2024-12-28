import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core';
import { ActivatableChannel, Channel, ChannelType, EMAIL } from '../../model';
import { ButtonSize } from '../../../../../shared';
import { BehaviorSubject } from 'rxjs';

@Component({
  selector: 'app-activatable-channel',
  templateUrl: './activatable-channel.component.html',
  styleUrls: ['./activatable-channel.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ActivatableChannelComponent implements OnDestroy {
  @Input()
  channel!: ActivatableChannel;

  @Output()
  updated = new EventEmitter<Channel>();

  @Output()
  activated = new EventEmitter<ChannelType>();

  @Output()
  deactivated = new EventEmitter<ChannelType>();

  protected readonly editing$ = new BehaviorSubject<boolean>(false);

  protected readonly EMAIL = EMAIL;
  protected readonly ButtonSize = ButtonSize;

  ngOnDestroy(): void {
    this.editing$.complete();
  }

  editChannel(): void {
    this.editing$.next(true);
  }

  cancelEditing(): void {
    this.editing$.next(false);
  }

  finishEditing(channel: Channel): void {
    this.updated.emit(channel);
    this.editing$.next(false);
  }

  toggleActivation(activated: boolean): void {
    if (activated) {
      this.activated.emit(this.channel.channel.type);
    } else {
      this.deactivated.emit(this.channel.channel.type);
    }
  }
}
