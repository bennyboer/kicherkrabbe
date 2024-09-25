import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import {
  DropdownComponent,
  DropdownItem,
  DropdownItemId,
} from '../../../../../shared';

@Component({
  selector: 'app-mailbox-page',
  templateUrl: './mailbox.page.html',
  styleUrls: ['./mailbox.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MailboxPage implements OnDestroy {
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

  mails = [
    {
      id: '1',
      subject: "I'm interested in your product",
      sender: {
        name: 'John Doe',
        mail: 'john.doe@example.com',
      },
      receivedAt: new Date('2024-09-21T12:30:00'),
      unread: true,
    },
    {
      id: '2',
      subject: 'How can I contact you?',
      sender: {
        name: 'Max Mustermann',
        mail: 'max.mustermann@example.com',
      },
      receivedAt: new Date('2024-09-20T17:45:00'),
      unread: false,
    },
    {
      id: '3',
      subject:
        'What about a fairly long subject that should be cut off before we get into trouble with the design?',
      sender: {
        name: 'Bennybör',
        mail: 'benjamin.barny.eder+eineechtlangemail@googlemail.com',
      },
      receivedAt: new Date('2024-09-20T17:45:00'),
      unread: false,
    },
  ];

  ngOnDestroy(): void {
    this.statusDropdownItems$.complete();
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
}
