import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { COMPONENTS, DialogOutletComponent } from './components';
import { DialogService } from './services';

@NgModule({
  imports: [CommonModule],
  declarations: [...COMPONENTS],
  exports: [DialogOutletComponent],
})
export class DialogModule {
  static forRoot() {
    return {
      ngModule: DialogModule,
      providers: [DialogService],
    };
  }

  static forChild() {
    return {
      ngModule: DialogModule,
      providers: [],
    };
  }
}
