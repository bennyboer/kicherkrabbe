import { NgModule } from '@angular/core';
import {
  AccordionComponent,
  AccordionItemComponent,
  ButtonColumnComponent,
  ButtonComponent,
  ButtonRowComponent,
  CardListComponent,
  CheckboxComponent,
  ChipComponent,
  ChipsComponent,
  ColorBadgeComponent,
  ColorPickerComponent,
  DropdownComponent,
  FileSelectComponent,
  FilterSortBarComponent,
  LoadingBarComponent,
  LoadingSpinnerComponent,
  MobileSwitchComponent,
  NoteComponent,
  NotificationOutletComponent,
  OverlayComponent,
  OverlayContainerComponent,
  PriceTagComponent,
  RadioComponent,
} from './components';
import { FlagService, NotificationService, OverlayService } from './services';
import { FlagDirective, UnlessFlagDirective } from './directives';
import { CommonModule } from '@angular/common';
import { NgxColorsModule } from 'ngx-colors';
import { RouterLink } from '@angular/router';
import { OptionModule } from './modules/option';

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
  LoadingBarComponent,
  NoteComponent,
  NotificationOutletComponent,
  ColorPickerComponent,
  ColorBadgeComponent,
  FileSelectComponent,
  ChipsComponent,
  ChipComponent,
];

const DIRECTIVES = [FlagDirective, UnlessFlagDirective];

@NgModule({
  declarations: [...COMPONENTS, ...DIRECTIVES],
  exports: [...COMPONENTS, FlagDirective, UnlessFlagDirective],
  imports: [CommonModule, NgxColorsModule, RouterLink, OptionModule],
})
export class SharedModule {
  static forRoot() {
    return {
      ngModule: SharedModule,
      providers: [FlagService, OverlayService, NotificationService],
    };
  }

  static forChild() {
    return {
      ngModule: SharedModule,
      providers: [],
    };
  }
}
