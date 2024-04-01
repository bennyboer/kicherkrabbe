import { ButtonComponent, Size } from './button.component';
import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, map, Observable } from 'rxjs';

export class RegisteredButton {
  readonly id: ButtonId;
  readonly index: number;
  readonly component: ButtonComponent;

  private constructor(props: {
    id: ButtonId;
    index: number;
    component: ButtonComponent;
  }) {
    this.id = props.id;
    this.index = props.index;
    this.component = props.component;
  }

  static create(index: number, component: ButtonComponent): RegisteredButton {
    const id: ButtonId = crypto.randomUUID();

    return new RegisteredButton({
      id,
      index,
      component,
    });
  }
}

export interface RegisteredButtonsLookup {
  [id: string]: RegisteredButton;
}

export type ButtonId = string;

@Injectable()
export abstract class ButtonRegistry implements OnDestroy {
  private readonly lookup$: BehaviorSubject<RegisteredButtonsLookup> =
    new BehaviorSubject<RegisteredButtonsLookup>({});

  private readonly size$: BehaviorSubject<Size> = new BehaviorSubject<Size>(
    Size.NORMAL,
  );

  private readonly firstRounded$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);
  private readonly lastRounded$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);

  ngOnDestroy(): void {
    this.lookup$.complete();
    this.size$.complete();
    this.firstRounded$.complete();
    this.lastRounded$.complete();
  }

  protected abstract afterRegister(button: RegisteredButton): void;

  register(button: ButtonComponent): ButtonId {
    const currentLookup = this.getCurrentLookup();
    const index = Object.keys(currentLookup).length;

    const registeredButton = RegisteredButton.create(index, button);

    const updatedLookup = { ...currentLookup };
    updatedLookup[registeredButton.id] = registeredButton;
    this.updateLookup(updatedLookup);

    this.afterRegister(registeredButton);

    return registeredButton.id;
  }

  unregister(buttonId: ButtonId): void {
    const currentLookup = this.getCurrentLookup();
    const updatedLookup = { ...currentLookup };
    delete updatedLookup[buttonId];
    this.updateLookup(updatedLookup);
  }

  isFirst(buttonId: ButtonId): Observable<boolean> {
    return this.lookup$
      .asObservable()
      .pipe(map((lookup) => lookup[buttonId]?.index === 0));
  }

  isLast(buttonId: ButtonId): Observable<boolean> {
    return this.lookup$
      .asObservable()
      .pipe(
        map(
          (lookup) =>
            lookup[buttonId]?.index === Object.keys(lookup).length - 1,
        ),
      );
  }

  isFirstRounded(): Observable<boolean> {
    return this.firstRounded$.asObservable();
  }

  isLastRounded(): Observable<boolean> {
    return this.lastRounded$.asObservable();
  }

  getSize(): Observable<Size> {
    return this.size$.asObservable();
  }

  setSize(size: Size): void {
    this.size$.next(size);
  }

  setFirstRounded(rounded: boolean): void {
    this.firstRounded$.next(rounded);
  }

  setLastRounded(rounded: boolean): void {
    this.lastRounded$.next(rounded);
  }

  protected getCurrentLookup(): RegisteredButtonsLookup {
    return this.lookup$.value;
  }

  protected updateLookup(lookup: RegisteredButtonsLookup): void {
    this.lookup$.next(lookup);
  }
}
