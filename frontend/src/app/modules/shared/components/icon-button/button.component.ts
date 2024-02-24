import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

export enum ButtonSize {
  SMALL = 'SMALL',
  MEDIUM = 'MEDIUM',
  LARGE = 'LARGE',
}

@Component({
  selector: 'app-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '[class]': 'sizeClass',
  },
})
export class ButtonComponent {
  @Input()
  icon: string | null = null;

  @Input()
  size: ButtonSize = ButtonSize.MEDIUM;

  get sizeClass(): string {
    return this.getClassForSize(this.size);
  }

  private getClassForSize(size: ButtonSize): string {
    switch (size) {
      case ButtonSize.SMALL:
        return 'small';
      case ButtonSize.MEDIUM:
        return 'medium';
      case ButtonSize.LARGE:
        return 'large';
      default:
        throw new Error(`Unknown size: ${size}`);
    }
  }
}
