import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  numberAttribute,
  OnDestroy,
  Output,
  ViewChild,
} from '@angular/core';
import { BehaviorSubject, Observable, Subject, takeUntil } from 'rxjs';
import { NgxColorsTriggerDirective } from 'ngx-colors';
import { someOrNone } from '@kicherkrabbe/shared';

export interface ColorPickerColor {
  red: number;
  green: number;
  blue: number;
}

const DEFAULT_COLOR: ColorPickerColor = {
  red: 255,
  green: 0,
  blue: 0,
};

@Component({
  selector: 'app-color-picker',
  templateUrl: './color-picker.component.html',
  styleUrls: ['./color-picker.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class ColorPickerComponent implements AfterViewInit, OnDestroy {
  @ViewChild(NgxColorsTriggerDirective)
  triggerDirective!: NgxColorsTriggerDirective;

  private readonly color$: BehaviorSubject<ColorPickerColor> = new BehaviorSubject<ColorPickerColor>(DEFAULT_COLOR);
  private readonly size$: BehaviorSubject<number> = new BehaviorSubject<number>(32);
  private readonly destroy$: Subject<void> = new Subject<void>();

  @Input({ transform: numberAttribute })
  set size(value: number) {
    this.size$.next(value);
  }

  @Input()
  set color(value: ColorPickerColor | null) {
    someOrNone(value).ifSome((color) => this.color$.next(color));
  }

  @Output()
  colorChanged: EventEmitter<ColorPickerColor> = new EventEmitter<ColorPickerColor>();

  ngAfterViewInit(): void {
    this.triggerDirective.acceptLabel = 'Fertig';
    this.triggerDirective.cancelLabel = 'Abbrechen';
    this.triggerDirective.colorPickerControls = 'no-alpha';
    this.triggerDirective.hideTextInput = true;

    this.triggerDirective.change.pipe(takeUntil(this.destroy$)).subscribe((color) => {
      const red = parseInt(color.substring(1, 3), 16);
      const green = parseInt(color.substring(3, 5), 16);
      const blue = parseInt(color.substring(5, 7), 16);
      const updatedColor = { red, green, blue };

      this.color$.next(updatedColor);
      this.colorChanged.emit(updatedColor);
    });
  }

  ngOnDestroy(): void {
    this.color$.complete();
    this.size$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  getColor(): Observable<ColorPickerColor> {
    return this.color$.asObservable();
  }

  getSize(): Observable<number> {
    return this.size$.asObservable();
  }
}
