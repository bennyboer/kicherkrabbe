import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotFoundPage, StartPage } from './pages';

const routes: Routes = [
  {
    path: '',
    component: StartPage,
  },
  {
    path: 'admin',
    title: 'Verwaltung',
    loadChildren: () => import('./modules/admin/admin.module').then((m) => m.AdminModule),
  },
  {
    path: 'catalog',
    title: 'Katalog',
    loadChildren: () => import('./modules/catalog/catalog.module').then((m) => m.CatalogModule),
  },
  {
    path: 'contact',
    title: 'Kontakt',
    loadChildren: () => import('./modules/contact/contact.module').then((m) => m.ContactModule),
    data: {
      description: 'Kontaktiere uns bei Fragen, Bestellungen oder Anregungen zu handgemachter Kinder- und Babykleidung',
    },
  },
  {
    path: 'legal',
    title: 'Rechtliches',
    loadChildren: () => import('./modules/legal/legal.module').then((m) => m.LegalModule),
  },
  {
    path: 'landing',
    title: 'Startseiten',
    loadChildren: () => import('./modules/landing/landing.module').then((m) => m.LandingModule),
  },
  { path: '**', component: NotFoundPage },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, {
      paramsInheritanceStrategy: 'always',
      scrollPositionRestoration: 'enabled',
    }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
