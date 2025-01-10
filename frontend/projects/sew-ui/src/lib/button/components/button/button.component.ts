import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'sew-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class ButtonComponent {}
