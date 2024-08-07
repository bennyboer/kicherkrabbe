import { inject, NgModule } from '@angular/core';
import { Router, RouterModule, Routes } from '@angular/router';
import { DashboardPage, LoginPage } from './pages';
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
        component: DashboardPage,
      },
      {
        path: 'topics',
        loadChildren: () =>
          import('./modules/topics/topics.module').then((m) => m.TopicsModule),
      },
      {
        path: 'fabric-types',
        loadChildren: () =>
          import('./modules/fabric-types/fabric-types.module').then(
            (m) => m.FabricTypesModule,
          ),
      },
      {
        path: 'colors',
        loadChildren: () =>
          import('./modules/colors/colors.module').then((m) => m.ColorsModule),
      },
      {
        path: 'fabrics',
        loadChildren: () =>
          import('./modules/fabrics/fabrics.module').then(
            (m) => m.FabricsModule,
          ),
      },
      {
        path: 'patterns',
        loadChildren: () =>
          import('./modules/patterns/patterns.module').then(
            (m) => m.PatternsModule,
          ),
      },
      {
        path: 'categories',
        loadChildren: () =>
          import('./modules/categories/categories.module').then(
            (m) => m.CategoriesModule,
          ),
      },
    ],
  },
  {
    path: 'login',
    component: LoginPage,
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}
