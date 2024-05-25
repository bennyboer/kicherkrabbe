import { inject, NgModule } from '@angular/core';
import { Router, RouterModule, Routes } from '@angular/router';
import { DashboardPage, LoginPage } from './pages';
import { AdminAuthService } from './services';
import { map } from 'rxjs';

const routes: Routes = [
  {
    path: '',
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
