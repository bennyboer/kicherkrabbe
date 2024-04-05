import {
  ChangeDetectionStrategy,
  Component,
  HostBinding,
  Injector,
  Input,
} from '@angular/core';
import { ButtonRowService } from './button-row.service';
import { Size } from '../button/button.component';
import { ButtonRegistry } from '../button/button-registry';

@Component({
  selector: 'app-button-row',
  templateUrl: './button-row.component.html',
  styleUrls: ['./button-row.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [
    {
      provide: ButtonRegistry,
      useClass: ButtonRowService,
    },
  ],
})
export class ButtonRowComponent {
  @Input('size')
  set setSize(size: Size) {
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
