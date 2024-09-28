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
  Observable,
  of,
  Subject,
  takeUntil,
} from 'rxjs';
import {
  DropdownComponent,
  DropdownItem,
  DropdownItemId,
} from '../../../../../shared';
import { Mail, Sender } from '../../model';

@Component({
  selector: 'app-mailbox-page',
  templateUrl: './mailbox.page.html',
  styleUrls: ['./mailbox.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MailboxPage implements OnInit, OnDestroy {
  protected readonly mails$: BehaviorSubject<Mail[]> = new BehaviorSubject<
    Mail[]
  >([]);
  private readonly loadingMails$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);
  protected readonly mailsLoaded$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  protected readonly loading$: Observable<boolean> =
    this.loadingMails$.asObservable();

  private readonly destroy$: Subject<void> = new Subject<void>();

  protected readonly statusDropdownItems$: BehaviorSubject<DropdownItem[]> =
    new BehaviorSubject<DropdownItem[]>([
      {
        id: 'read',
        label: 'Gelesen',
      },
      {
        id: 'unread',
        label: 'Ungelesen',
      },
    ]);

  ngOnInit(): void {
    this.reloadMails();
  }

  ngOnDestroy(): void {
    this.statusDropdownItems$.complete();
    this.mails$.complete();
    this.loadingMails$.complete();
    this.mailsLoaded$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateSearchTerm(value: string): void {
    console.log('searchTerm', value);
  }

  updateSelectedStatus(
    dropdown: DropdownComponent,
    items: DropdownItemId[],
  ): void {
    if (items.length === 0) {
      // TODO Reset selected status
      console.log('noStatusSelected');
      return;
    }

    // TODO Select status
    console.log('selectedStatus', items);

    dropdown.toggleOpened();
  }

  clearStatusSelection(dropdown: DropdownComponent): void {
    dropdown.clearSelection();
    dropdown.toggleOpened();
  }

  private reloadMails(): void {
    this.loadingMails$.next(true);

    const MOCK_MAILS = [
      Mail.of({
        id: '1',
        subject: "I'm interested in your product",
        sender: Sender.of({
          name: 'John Doe',
          mail: 'john.doe@example.com',
        }),
        receivedAt: new Date('2024-09-21T12:30:00'),
        read: false,
      }),
      Mail.of({
        id: '2',
        subject: 'How can I contact you?',
        sender: Sender.of({
          name: 'Max Mustermann',
          mail: 'max.mustermann@example.com',
        }),
        receivedAt: new Date('2024-09-20T17:45:00'),
        read: true,
      }),
      Mail.of({
        id: '3',
        subject:
          'What about a fairly long subject that should be cut off before we get into trouble with the design?',
        sender: Sender.of({
          name: 'Bennybör',
          mail: 'benjamin.barny.eder+eineechtlangemail@googlemail.com',
        }),
        receivedAt: new Date('2024-09-20T17:45:00'),
        read: true,
      }),
    ];

    // TODO Load mails from backend
    of(MOCK_MAILS)
      .pipe(
        delay(1000),
        first(),
        catchError((e) => {
          console.error('Error loading mails', e);
          return of([]);
        }),
        takeUntil(this.destroy$),
        finalize(() => {
          this.loadingMails$.next(false);
          this.mailsLoaded$.next(true);
        }),
      )
      .subscribe((mails) => this.mails$.next(mails));
  }
}
