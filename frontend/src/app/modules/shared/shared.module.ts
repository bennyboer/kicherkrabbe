import { NgModule } from '@angular/core';
import {
  AccordionComponent,
  AccordionItemComponent,
  CardListComponent,
  PriceTagComponent,
  SortSelectorComponent,
} from './components';
import { FlagService } from './services';
import { FlagDirective, UnlessFlagDirective } from './directives';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

const COMPONENTS = [
  PriceTagComponent,
  CardListComponent,
  AccordionComponent,
  AccordionItemComponent,
  SortSelectorComponent,
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
      providers: [FlagService],
    };
  }

  static forChild() {
    return {
      ngModule: SharedModule,
      providers: [],
    };
  }
}
