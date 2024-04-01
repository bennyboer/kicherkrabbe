import { Injectable, OnDestroy } from '@angular/core';
import { ButtonRegistry, RegisteredButton } from '../button/button-registry';

@Injectable()
export class ButtonRowService extends ButtonRegistry implements OnDestroy {
  protected afterRegister(button: RegisteredButton) {
    button.component.addClass('in-button-row');
    button.component.addClass(`button-row-item-${button.index}`);
  }
}
