import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    selector: 'app-terms-and-conditions-page',
    templateUrl: './terms-and-conditions.page.html',
    styleUrls: ['./terms-and-conditions.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class TermsAndConditionsPage {}
