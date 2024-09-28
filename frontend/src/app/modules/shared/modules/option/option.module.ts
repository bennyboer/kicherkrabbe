import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IfNonePipe, IfSomePipe, PIPES } from './pipes';

@NgModule({
  imports: [CommonModule],
  declarations: [...PIPES],
  exports: [IfSomePipe, IfNonePipe],
})
export class OptionModule {}
