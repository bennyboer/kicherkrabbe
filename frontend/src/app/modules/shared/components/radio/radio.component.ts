import {
  booleanAttribute,
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  Output,
} from '@angular/core';
import { Option } from '../../../../util';
import { BehaviorSubject, Observable, ReplaySubject, Subject } from 'rxjs';

@Component({
  selector: 'app-radio',
  templateUrl: './radio.component.html',
  styleUrls: ['./radio.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RadioComponent implements OnDestroy {
  @Input()
  set label(value: string) {
    Option.someOrNone(value).ifSome((l) => this.label$.next(l));
  }

  @Input({ transform: booleanAttribute, alias: 'checked' })
  set checked(value: boolean) {
    Option.someOrNone(value).ifSome((c) => this.checked$.next(c));
  }

  @Input()
  set passive(value: boolean) {
    Option.someOrNone(value).ifSome((p) => this.passive$.next(p));
  }

  @Output()
  checkedChanges: EventEmitter<boolean> = new EventEmitter<boolean>();

  protected readonly id: string = crypto.randomUUID();
  private readonly label$: Subject<string> = new ReplaySubject<string>(1);
  private readonly checked$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly passive$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);

  ngOnDestroy(): void {
    this.label$.complete();
    this.checked$.complete();
    this.passive$.complete();
  }

  isChecked(): Observable<boolean> {
    return this.checked$.asObservable();
  }

  getLabel(): Observable<string> {
    return this.label$.asObservable();
  }

  protected onRadioClicked(event: MouseEvent): void {
    const isPassive = this.passive$.value;

    if (!isPassive) {
      const newChecked = !this.checked$.value;
      this.checked$.next(newChecked);
      this.checkedChanges.emit(newChecked);
    }
  }
}
