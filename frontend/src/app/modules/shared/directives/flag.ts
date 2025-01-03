import { Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';
import { Flag, FlagService } from '../services';

@Directive({
    selector: '[flag]',
    standalone: false
})
export class FlagDirective {
  private hasView = false;

  constructor(
    private readonly templateRef: TemplateRef<any>,
    private readonly viewContainer: ViewContainerRef,
    private readonly flagService: FlagService,
  ) {}

  @Input()
  set flag(flag: Flag | string) {
    let realFlag: Flag;
    if (typeof flag === 'string') {
      realFlag = Flag[flag as keyof typeof Flag];
    } else {
      realFlag = flag;
    }

    const isActive = this.flagService.isActive(realFlag);

    if (isActive && !this.hasView) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if (!isActive && this.hasView) {
      this.viewContainer.clear();
      this.hasView = false;
    }
  }
}
