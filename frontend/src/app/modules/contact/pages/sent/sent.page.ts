import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    selector: 'app-sent-page',
    templateUrl: './sent.page.html',
    styleUrls: ['./sent.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class SentPage {}
