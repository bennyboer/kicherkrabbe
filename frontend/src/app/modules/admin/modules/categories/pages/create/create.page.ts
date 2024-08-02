import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-create-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreatePage {}
