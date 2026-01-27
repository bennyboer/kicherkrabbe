import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    selector: 'app-privacy-policy-page',
    templateUrl: './privacy-policy.page.html',
    styleUrls: ['./privacy-policy.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class PrivacyPolicyPage {}
