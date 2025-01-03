import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-delete-page',
  templateUrl: './delete.page.html',
  styleUrls: ['./delete.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class DeletePage {}
