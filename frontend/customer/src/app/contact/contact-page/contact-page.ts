import { ChangeDetectionStrategy, Component } from '@angular/core';
import { InquirySection } from '../inquiry-section/inquiry-section';

@Component({
  selector: 'app-contact-page',
  templateUrl: './contact-page.html',
  styleUrl: './contact-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [InquirySection],
})
export class ContactPage {}
