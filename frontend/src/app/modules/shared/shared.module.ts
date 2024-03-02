import { NgModule } from '@angular/core';
import {
  ButtonComponent,
  CardListComponent,
  PriceTagComponent,
} from './components';
import { FlagService } from './services';
import { FlagDirective, UnlessFlagDirective } from './directives';
import { RouterLink } from '@angular/router';

const COMPONENTS = [ButtonComponent, PriceTagComponent, CardListComponent];

const DIRECTIVES = [FlagDirective, UnlessFlagDirective];

@NgModule({
  declarations: [...COMPONENTS, ...DIRECTIVES],
  exports: [...COMPONENTS, FlagDirective, UnlessFlagDirective],
  imports: [RouterLink],
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
