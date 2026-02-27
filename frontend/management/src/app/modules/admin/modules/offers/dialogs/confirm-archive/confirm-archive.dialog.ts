import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';

export interface ConfirmArchiveDialogResult {
  confirmed: boolean;
}

@Component({
  selector: 'app-confirm-archive-dialog',
  templateUrl: './confirm-archive.dialog.html',
  styleUrls: ['./confirm-archive.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class ConfirmArchiveDialog {
  constructor(
    private readonly dialog: Dialog<ConfirmArchiveDialogResult>,
    private readonly dialogService: DialogService,
  ) {}

  cancel(): void {
    this.dialogService.close(this.dialog.id);
  }

  confirm(): void {
    this.dialog.attachResult({ confirmed: true });
    this.dialogService.close(this.dialog.id);
  }
}
