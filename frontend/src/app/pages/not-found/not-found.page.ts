import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    selector: 'app-not-found-page',
    templateUrl: './not-found.page.html',
    styleUrls: ['./not-found.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class NotFoundPage {}
