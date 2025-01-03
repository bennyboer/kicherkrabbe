import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { ProductsRoutingModule } from './products-routing.module';
import { PAGES } from './pages';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { SERVICES } from './services';

@NgModule({
  imports: [CommonModule, ProductsRoutingModule, SharedModule],
  declarations: [...PAGES],
  providers: [provideHttpClient(withInterceptorsFromDi()), ...SERVICES],
})
export class ProductsModule {}
