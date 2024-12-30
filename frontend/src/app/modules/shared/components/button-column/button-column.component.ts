import { ChangeDetectionStrategy, Component, HostBinding, Injector, Input } from '@angular/core';
import { ButtonColumnService } from './button-column.service';
import { ButtonSize } from '../button/button.component';
import { ButtonRegistry } from '../button/button-registry';

@Component({
  selector: 'app-button-column',
  templateUrl: './button-column.component.html',
  styleUrls: ['./button-column.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: ButtonRegistry,
      useClass: ButtonColumnService,
    },
  ],
})
export class ButtonColumnComponent {
  @Input('size')
  set setSize(size: ButtonSize) {
    this.buttonRegistry.setSize(size);
  }

  @HostBinding('class.rounded')
  @Input()
  rounded: boolean = true;

  constructor(
    private readonly buttonRegistry: ButtonRegistry,
    private readonly injector: Injector,
  ) {}

  getInjector(): Injector {
    return this.injector;
  }
}
