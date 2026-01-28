import { NgModule } from '@angular/core';
import { TestRoutingModule } from './test-routing.module';
import { CommonModule } from '@angular/common';
import { SewButtonModule } from 'sew-ui';
import { PAGES } from './pages';

@NgModule({
  imports: [CommonModule, TestRoutingModule, SewButtonModule],
  declarations: [...PAGES],
})
export class TestModule {}
