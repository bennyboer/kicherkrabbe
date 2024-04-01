import { Injectable, OnDestroy } from '@angular/core';
import { ButtonRegistry, RegisteredButton } from '../button/button-registry';

@Injectable()
export class ButtonColumnService extends ButtonRegistry implements OnDestroy {
  protected afterRegister(button: RegisteredButton) {
    button.component.addClass('in-button-column');
    button.component.addClass(`button-column-item-${button.index}`);
  }
}
