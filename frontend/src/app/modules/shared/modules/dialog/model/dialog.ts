import { validateProps } from '../../../../../util';
import { ComponentType } from '@angular/cdk/overlay';
import { Injector } from '@angular/core';
import { none, Option, someOrNone } from '../../option';

export class Dialog<R> {
  readonly id: string;
  readonly title: string;
  readonly componentType: ComponentType<any>;
  readonly injector: Injector;
  private result: Option<R> = none();

  private constructor(props: { id: string; title: string; componentType: ComponentType<any>; injector: Injector }) {
    validateProps(props);

    this.id = props.id;
    this.title = props.title;
    this.componentType = props.componentType;
    this.injector = props.injector;
  }

  static of<R>(props: { id: string; title: string; componentType: ComponentType<any>; injector: Injector }): Dialog<R> {
    return new Dialog({
      id: props.id,
      title: props.title,
      componentType: props.componentType,
      injector: props.injector,
    });
  }

  static create<R>(props: { title: string; componentType: ComponentType<any>; injector: Injector }): Dialog<R> {
    return this.of({
      id: crypto.randomUUID(),
      title: props.title,
      componentType: props.componentType,
      injector: props.injector,
    });
  }

  attachResult(result: R): void {
    this.result = someOrNone(result);
  }

  getResult(): Option<R> {
    return this.result;
  }

  getInjector(): Injector {
    return Injector.create({
      providers: [
        {
          provide: Dialog,
          useValue: this,
        },
      ],
      parent: this.injector,
    });
  }
}
