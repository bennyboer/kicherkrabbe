import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  BehaviorSubject,
  catchError,
  delay,
  finalize,
  first,
  map,
  Observable,
  of,
  Subject,
  takeUntil,
} from 'rxjs';
import { Mail, Sender } from '../../model';
import { none, Option, some } from '../../../../../shared/modules/option';

@Component({
  selector: 'app-mail-page',
  templateUrl: './mail.page.html',
  styleUrls: ['./mail.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MailPage implements OnInit, OnDestroy {
  protected readonly mail$: BehaviorSubject<Option<Mail>> = new BehaviorSubject<
    Option<Mail>
  >(none());
  private readonly loadingMail$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);
  protected readonly mailLoaded$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  protected readonly loading$: Observable<boolean> =
    this.loadingMail$.asObservable();

  private readonly destroy$: Subject<void> = new Subject<void>();

  ngOnInit(): void {
    this.reloadMail();
  }

  ngOnDestroy(): void {
    this.mail$.complete();
    this.mailLoaded$.complete();
    this.loadingMail$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  private reloadMail(): void {
    this.loadingMail$.next(true);

    // TODO Implement loading mail from backend
    of(
      Mail.of({
        id: '1',
        subject: "I'm interested in your product",
        content: 'Hello, I would like to know more about your product.',
        sender: Sender.of({
          name: 'John Doe',
          mail: 'john.doe@example.com',
        }),
        receivedAt: new Date('2024-09-21T12:30:00'),
        read: false,
      }),
    )
      .pipe(
        delay(1000),
        first(),
        map((mail) => some(mail)),
        catchError((e) => {
          console.error('Failed to load mail', e);
          return of(none<Mail>());
        }),
        takeUntil(this.destroy$),
        finalize(() => {
          this.loadingMail$.next(false);
          this.mailLoaded$.next(true);
        }),
      )
      .subscribe((mail) => this.mail$.next(mail));
  }
}
