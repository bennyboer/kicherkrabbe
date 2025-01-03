import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    selector: 'app-imprint-page',
    templateUrl: './imprint.page.html',
    styleUrls: ['./imprint.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class ImprintPage {}
