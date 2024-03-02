import { NgModule } from '@angular/core';
import { CatalogRoutingModule } from './catalog-routing.module';
import { CatalogPage } from './pages';
import { SharedModule } from '../shared/shared.module';

const PAGES = [CatalogPage];

@NgModule({
  declarations: [...PAGES],
  imports: [CatalogRoutingModule, SharedModule],
})
export class CatalogModule {}
