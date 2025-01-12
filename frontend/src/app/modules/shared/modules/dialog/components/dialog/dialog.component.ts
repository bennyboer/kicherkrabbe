import { ChangeDetectionStrategy, Component, Input, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { Dialog } from '../../model';
import { DialogService } from '../../services';

@Component({
  selector: 'app-dialog',
  templateUrl: './dialog.component.html',
  styleUrls: ['./dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class DialogComponent implements OnInit {
  @ViewChild('outlet', { static: true, read: ViewContainerRef })
  outlet!: ViewContainerRef;

  @Input({ required: true })
  dialog!: Dialog<any>;

  constructor(private readonly dialogService: DialogService) {}

  ngOnInit(): void {
    this.outlet.createComponent(this.dialog.componentType, {
      injector: this.dialog.getInjector(),
    });
  }

  close(): void {
    this.dialogService.close(this.dialog.id);
  }
}
