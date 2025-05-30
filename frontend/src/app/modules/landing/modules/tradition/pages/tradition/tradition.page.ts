import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    selector: 'app-tradition-page',
    templateUrl: './tradition.page.html',
    styleUrls: ['./tradition.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class TraditionPage {}
