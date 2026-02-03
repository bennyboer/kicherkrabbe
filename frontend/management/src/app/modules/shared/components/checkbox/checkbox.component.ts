import {
  booleanAttribute,
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  Output,
} from '@angular/core';
import { BehaviorSubject, Observable, ReplaySubject, Subject } from 'rxjs';
import { someOrNone } from '@kicherkrabbe/shared';

@Component({
  selector: 'app-checkbox',
  templateUrl: './checkbox.component.html',
  styleUrls: ['./checkbox.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class CheckboxComponent implements OnDestroy {
  @Input()
  set label(value: string) {
    someOrNone(value).ifSome((l) => this.label$.next(l));
  }

  @Input({ transform: booleanAttribute, alias: 'checked' })
  set checked(value: boolean) {
    someOrNone(value).ifSome((c) => this.checked$.next(c));
  }

  @Input()
  set passive(value: boolean) {
    someOrNone(value).ifSome((p) => this.passive$.next(p));
  }

  @Input({ transform: booleanAttribute })
  set disabled(value: boolean) {
    someOrNone(value).ifSome((d) => this.disabled$.next(d));
  }

  @Output()
  checkedChanges: EventEmitter<boolean> = new EventEmitter<boolean>();

  protected readonly id: string = crypto.randomUUID();
  private readonly label$: Subject<string> = new ReplaySubject<string>(1);
  protected readonly checked$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly passive$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly disabled$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  ngOnDestroy(): void {
    this.label$.complete();
    this.checked$.complete();
    this.passive$.complete();
    this.disabled$.complete();
  }

  getLabel(): Observable<string> {
    return this.label$.asObservable();
  }

  protected onCheckboxClicked(_event: MouseEvent): void {
    const isPassive = this.passive$.value;

    if (!isPassive) {
      const newChecked = !this.checked$.value;
      this.checked$.next(newChecked);
      this.checkedChanges.emit(newChecked);
    }
  }
}
