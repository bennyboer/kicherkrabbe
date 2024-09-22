import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-mailbox-page',
  templateUrl: './mailbox.page.html',
  styleUrls: ['./mailbox.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MailboxPage {
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
  ];
}
