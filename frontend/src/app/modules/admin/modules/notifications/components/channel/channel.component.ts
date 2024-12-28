import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { ActivatableChannel } from '../../model';

@Component({
  selector: 'app-channel',
  templateUrl: './channel.component.html',
  styleUrls: ['./channel.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChannelComponent {
  @Input() channel!: ActivatableChannel;
}
