import { inject, NgModule } from '@angular/core';
import { Router, RouterModule, Routes } from '@angular/router';
import { LoginPage } from './pages';
import { AdminAuthService } from './services';
import { map } from 'rxjs';
import { ContainerComponent } from './components';

const routes: Routes = [
  {
    path: '',
    component: ContainerComponent,
    canActivate: [
      () => {
        const authService = inject(AdminAuthService);
        const router = inject(Router);

        return authService.isLoggedIn().pipe(
          map((loggedIn) => {
            if (!loggedIn) {
              return router.parseUrl('/admin/login');
            }

            return true;
          }),
        );
      },
    ],
    children: [
      {
        path: '',
        loadChildren: () =>
          import('./modules/dashboard/dashboard.module').then(
            (m) => m.DashboardModule,
          ),
      },
      {
        path: 'topics',
        title: 'Themenverwaltung',
        loadChildren: () =>
          import('./modules/topics/topics.module').then((m) => m.TopicsModule),
      },
      {
        path: 'fabric-types',
        title: 'Stoffartenverwaltung',
        loadChildren: () =>
          import('./modules/fabric-types/fabric-types.module').then(
            (m) => m.FabricTypesModule,
          ),
      },
      {
        path: 'colors',
        title: 'Farbenverwaltung',
        loadChildren: () =>
          import('./modules/colors/colors.module').then((m) => m.ColorsModule),
      },
      {
        path: 'fabrics',
        title: 'Stoffverwaltung',
        loadChildren: () =>
          import('./modules/fabrics/fabrics.module').then(
            (m) => m.FabricsModule,
          ),
      },
      {
        path: 'patterns',
        title: 'Schnittmuster-Verwaltung',
        loadChildren: () =>
          import('./modules/patterns/patterns.module').then(
            (m) => m.PatternsModule,
          ),
      },
      {
        path: 'categories',
        title: 'Kategorienverwaltung',
        loadChildren: () =>
          import('./modules/categories/categories.module').then(
            (m) => m.CategoriesModule,
          ),
      },
      {
        path: 'mailbox',
        title: 'Postfach',
        loadChildren: () =>
          import('./modules/mailbox/mailbox.module').then(
            (m) => m.MailboxModule,
          ),
      },
      {
        path: 'inquiries',
        title: 'Kontaktanfragen',
        loadChildren: () =>
          import('./modules/inquiries/inquiries.module').then(
            (m) => m.InquiriesModule,
          ),
      },
    ],
  },
  {
    path: 'login',
    title: 'Anmeldung für die Verwaltung',
    component: LoginPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
