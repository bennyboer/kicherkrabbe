import { ComponentType } from '@angular/cdk/overlay';
import { EnvironmentInjector, Injector, StaticProvider } from '@angular/core';
import { none, Option, someOrNone, validateProps } from '@kicherkrabbe/shared';

export class Dialog<R> {
  readonly id: string;
  readonly title: string;
  readonly componentType: ComponentType<any>;
  readonly providers: StaticProvider[];
  readonly environmentInjector: EnvironmentInjector | null;
  private result: Option<R> = none();

  private constructor(props: {
    id: string;
    title: string;
    componentType: ComponentType<any>;
    providers: StaticProvider[];
    environmentInjector: EnvironmentInjector | null;
  }) {
    validateProps(props);

    this.id = props.id;
    this.title = props.title;
    this.componentType = props.componentType;
    this.providers = props.providers;
    this.environmentInjector = props.environmentInjector;
  }

  static of<R>(props: {
    id: string;
    title: string;
    componentType: ComponentType<any>;
    providers: StaticProvider[];
    environmentInjector?: EnvironmentInjector;
  }): Dialog<R> {
    return new Dialog({
      id: props.id,
      title: props.title,
      componentType: props.componentType,
      providers: props.providers,
      environmentInjector: props.environmentInjector ?? null,
    });
  }

  static create<R>(props: {
    title: string;
    componentType: ComponentType<any>;
    providers: StaticProvider[];
    environmentInjector?: EnvironmentInjector;
  }): Dialog<R> {
    return this.of({
      id: crypto.randomUUID(),
      title: props.title,
      componentType: props.componentType,
      providers: props.providers,
      environmentInjector: props.environmentInjector,
    });
  }

  attachResult(result: R): void {
    this.result = someOrNone(result);
  }

  getResult(): Option<R> {
    return this.result;
  }

  getInjector(parent?: Injector): Injector {
    return Injector.create({
      providers: [
        {
          provide: Dialog,
          useValue: this,
        },
      ],
      parent: Injector.create({
        providers: this.providers,
        parent,
      }),
    });
  }
}
