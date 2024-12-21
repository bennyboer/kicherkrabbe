import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { COMPONENTS, LineGraphComponent } from './components';

@NgModule({
  imports: [CommonModule],
  declarations: [...COMPONENTS],
  exports: [LineGraphComponent],
})
export class LineGraphModule {}
