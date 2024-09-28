import { CategoryGroup } from './category-group';
import { validateProps } from '../../../../../util';
import { someOrNone } from '../../../../shared/modules/option';

export type CategoryId = string;

export class Category {
  readonly id: CategoryId;
  readonly version: number;
  readonly name: string;
  readonly group: CategoryGroup;
  readonly createdAt: Date;

  private constructor(props: {
    id: CategoryId;
    version: number;
    name: string;
    group: CategoryGroup;
    createdAt: Date;
  }) {
    validateProps(props);

    this.id = props.id;
    this.version = props.version;
    this.name = props.name;
    this.group = props.group;
    this.createdAt = props.createdAt;
  }

  static of(props: {
    id: CategoryId;
    version?: number;
    name: string;
    group: CategoryGroup;
    createdAt: Date;
  }): Category {
    return new Category({
      ...props,
      version: someOrNone(props.version).orElse(0),
    });
  }
}
