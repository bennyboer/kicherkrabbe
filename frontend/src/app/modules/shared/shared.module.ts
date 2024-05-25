import { NgModule } from '@angular/core';
import {
  AccordionComponent,
  AccordionItemComponent,
  ButtonColumnComponent,
  ButtonComponent,
  ButtonRowComponent,
  CardListComponent,
  CheckboxComponent,
  DropdownComponent,
  FilterSortBarComponent,
  LoadingSpinnerComponent,
  MobileSwitchComponent,
  OverlayComponent,
  OverlayContainerComponent,
  PriceTagComponent,
  RadioComponent,
} from './components';
import { FlagService, OverlayService } from './services';
import { FlagDirective, UnlessFlagDirective } from './directives';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

const COMPONENTS = [
  PriceTagComponent,
  CardListComponent,
  AccordionComponent,
  AccordionItemComponent,
  FilterSortBarComponent,
  DropdownComponent,
  ButtonComponent,
  ButtonRowComponent,
  ButtonColumnComponent,
  CheckboxComponent,
  RadioComponent,
  MobileSwitchComponent,
  OverlayContainerComponent,
  OverlayComponent,
  LoadingSpinnerComponent,
];

const DIRECTIVES = [FlagDirective, UnlessFlagDirective];

@NgModule({
  declarations: [...COMPONENTS, ...DIRECTIVES],
  exports: [...COMPONENTS, FlagDirective, UnlessFlagDirective],
  imports: [CommonModule, RouterLink],
})
export class SharedModule {
  static forRoot() {
    return {
      ngModule: SharedModule,
      providers: [FlagService, OverlayService],
    };
  }

  static forChild() {
    return {
      ngModule: SharedModule,
      providers: [],
    };
  }
}
