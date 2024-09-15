import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-inquiries-page',
  templateUrl: './inquiries.page.html',
  styleUrls: ['./inquiries.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InquiriesPage {}
