import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
    selector: 'app-admin-container',
    templateUrl: './container.component.html',
    styleUrls: ['./container.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class ContainerComponent {}
