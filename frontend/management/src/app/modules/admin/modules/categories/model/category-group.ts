import { validateProps } from '@kicherkrabbe/shared';

export enum CategoryGroupType {
  CLOTHING = 'CLOTHING',
  NONE = 'NONE',
}

export class CategoryGroup {
  readonly type: CategoryGroupType;
  readonly name: string;

  private constructor(props: { type: CategoryGroupType; name: string }) {
    validateProps(props);

    this.type = props.type;
    this.name = props.name;
  }

  static clothing(): CategoryGroup {
    return new CategoryGroup({
      type: CategoryGroupType.CLOTHING,
      name: 'Kleidung',
    });
  }

  static none(): CategoryGroup {
    return new CategoryGroup({
      type: CategoryGroupType.NONE,
      name: 'Keine',
    });
  }
}

export const CLOTHING: CategoryGroup = CategoryGroup.clothing();
export const NONE: CategoryGroup = CategoryGroup.none();

export const GROUPS: CategoryGroup[] = [CLOTHING, NONE];
