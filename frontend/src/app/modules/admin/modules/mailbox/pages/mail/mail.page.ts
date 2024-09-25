import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-mail-page',
  templateUrl: './mail.page.html',
  styleUrls: ['./mail.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MailPage {}
