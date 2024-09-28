import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import {
  BehaviorSubject,
  combineLatest,
  map,
  Observable,
  Subject,
  takeUntil,
} from 'rxjs';
import { AdminAuthService } from '../../services';
import { ActivatedRoute, Router } from '@angular/router';
import { someOrNone } from '../../../shared/modules/option';

@Component({
  selector: 'app-login-page',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPage implements OnDestroy {
  private readonly username$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');
  private readonly password$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');
  private readonly loggingIn$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly error$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly adminAuthService: AdminAuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
  ) {}

  ngOnDestroy(): void {
    this.username$.complete();
    this.password$.complete();
    this.loggingIn$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  isValid(): Observable<boolean> {
    return combineLatest([this.username$, this.password$]).pipe(
      map(([username, password]) =>
        this.isValidCredentials(username, password),
      ),
    );
  }

  cannotLogin(): Observable<boolean> {
    const invalid$ = this.isValid().pipe(map((isValid) => !isValid));
    const loading$ = this.isLoading();

    return combineLatest([invalid$, loading$]).pipe(
      map(([invalid, loading]) => invalid || loading),
    );
  }

  isLoading(): Observable<boolean> {
    return this.loggingIn$.asObservable();
  }

  isError(): Observable<boolean> {
    return this.error$.asObservable();
  }

  login(): void {
    const username = this.username$.value;
    const password = this.password$.value;

    this.error$.next(false);
    this.loggingIn$.next(true);

    this.adminAuthService
      .login(username, password)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const redirect = someOrNone(
            this.route.snapshot.queryParams['redirect'],
          ).orElse('/admin');

          this.router.navigate([redirect]);
        },
        error: () => {
          this.error$.next(true);
          this.loggingIn$.next(false);
        },
        complete: () => this.loggingIn$.next(false),
      });
  }

  updateUsername(value: string): void {
    this.username$.next(value);
  }

  updatePassword(value: string): void {
    this.password$.next(value);
  }

  private isValidCredentials(username: string, password: string): boolean {
    return this.isValidUsername(username) && this.isValidPassword(password);
  }

  private isValidUsername(username: string): boolean {
    return username.length >= 3;
  }

  private isValidPassword(password: string): boolean {
    return password.length >= 8;
  }
}
