import { Injectable } from '@angular/core';
import { Dialog } from '../model';
import { BehaviorSubject, filter, first, map, Observable } from 'rxjs';

@Injectable()
export class DialogService {
  private readonly dialogs$ = new BehaviorSubject<Dialog<any>[]>([]);

  getDialogs(): Observable<Dialog<any>[]> {
    return this.dialogs$.asObservable();
  }

  open(dialog: Dialog<any>): void {
    this.dialogs$.next([...this.dialogs$.value, dialog]);
  }

  close(dialogId: string): void {
    const updatedDialogs = this.dialogs$.value.filter((dialog) => dialog.id !== dialogId);
    this.dialogs$.next(updatedDialogs);
  }

  waitUntilClosed(dialogId: string): Observable<void> {
    return this.dialogs$.asObservable().pipe(
      map((dialogs) => dialogs.find((dialog) => dialog.id === dialogId)),
      filter((dialog) => !dialog),
      first(),
    );
  }
}
