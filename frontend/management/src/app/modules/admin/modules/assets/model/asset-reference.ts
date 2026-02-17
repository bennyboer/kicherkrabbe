import { validateProps } from '@kicherkrabbe/shared';

export class AssetReference {
  readonly resourceType: string;
  readonly resourceId: string;
  readonly resourceName: string;

  private constructor(props: { resourceType: string; resourceId: string; resourceName: string }) {
    validateProps(props);

    this.resourceType = props.resourceType;
    this.resourceId = props.resourceId;
    this.resourceName = props.resourceName;
  }

  static of(props: { resourceType: string; resourceId: string; resourceName: string }): AssetReference {
    return new AssetReference({
      resourceType: props.resourceType,
      resourceId: props.resourceId,
      resourceName: props.resourceName,
    });
  }
}
