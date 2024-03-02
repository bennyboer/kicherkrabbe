import { Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';
import { Flag, FlagService } from '../services';

@Directive({
  selector: '[unlessFlag]',
})
export class UnlessFlagDirective {
  private hasView = false;

  constructor(
    private readonly templateRef: TemplateRef<any>,
    private readonly viewContainer: ViewContainerRef,
    private readonly flagService: FlagService,
  ) {}

  @Input()
  set unlessFlag(flag: Flag | string) {
    let realFlag: Flag;
    if (typeof flag === 'string') {
      realFlag = Flag[flag as keyof typeof Flag];
    } else {
      realFlag = flag;
    }

    const shouldDisplay = !this.flagService.isActive(realFlag);

    if (shouldDisplay && !this.hasView) {
      this.viewContainer.createEmbeddedView(this.templateRef);
      this.hasView = true;
    } else if (!shouldDisplay && this.hasView) {
      this.viewContainer.clear();
      this.hasView = false;
    }
  }
}
