import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, Renderer2 } from '@angular/core';
import { DialogService } from '../../services';
import { BehaviorSubject, map, Subject, takeUntil } from 'rxjs';
import { Dialog } from '../../model';

@Component({
  selector: 'app-dialog-outlet',
  templateUrl: './outlet.component.html',
  styleUrls: ['./outlet.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class DialogOutletComponent implements OnDestroy, OnInit {
  protected readonly dialogs$ = new BehaviorSubject<Dialog<any>[]>([]);

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly dialogService: DialogService,
    private readonly renderer: Renderer2,
  ) {}

  ngOnInit(): void {
    this.dialogService
      .getDialogs()
      .pipe(takeUntil(this.destroy$))
      .subscribe((dialogs) => this.dialogs$.next(dialogs));

    const atLeastOneDialogOpen$ = this.dialogs$
      .pipe(map((dialogs) => dialogs.length > 0))
      .subscribe((atLeastOneDialogOpen) => {
        if (atLeastOneDialogOpen) {
          this.renderer.addClass(document.body, 'prevent-scrolling');
        } else {
          this.renderer.removeClass(document.body, 'prevent-scrolling');
        }
      });
  }

  ngOnDestroy(): void {
    this.dialogs$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }
}
