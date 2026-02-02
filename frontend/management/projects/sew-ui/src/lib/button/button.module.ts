import { NgModule } from '@angular/core';
import { ButtonComponent, COMPONENTS } from './components';

@NgModule({
  declarations: [...COMPONENTS],
  exports: [ButtonComponent],
})
export class SewButtonModule {}
