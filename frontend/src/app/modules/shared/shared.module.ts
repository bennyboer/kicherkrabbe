import { NgModule } from '@angular/core';
import { ButtonComponent } from './components';

const COMPONENTS = [ButtonComponent];

@NgModule({
  declarations: [...COMPONENTS],
  exports: [...COMPONENTS],
})
export class SharedModule {}
