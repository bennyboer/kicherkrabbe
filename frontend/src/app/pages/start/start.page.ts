import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    selector: 'app-start-page',
    templateUrl: './start.page.html',
    styleUrls: ['./start.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class StartPage {}
