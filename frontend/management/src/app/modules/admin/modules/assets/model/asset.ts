import { validateProps } from '@kicherkrabbe/shared';
import { AssetReference } from './asset-reference';

export class Asset {
  readonly id: string;
  readonly version: number;
  readonly contentType: string;
  readonly fileSize: number;
  readonly createdAt: Date;
  readonly references: AssetReference[];

  private constructor(props: {
    id: string;
    version: number;
    contentType: string;
    fileSize: number;
    createdAt: Date;
    references: AssetReference[];
  }) {
    validateProps(props);

    this.id = props.id;
    this.version = props.version;
    this.contentType = props.contentType;
    this.fileSize = props.fileSize;
    this.createdAt = props.createdAt;
    this.references = props.references;
  }

  static of(props: {
    id: string;
    version: number;
    contentType: string;
    fileSize: number;
    createdAt: Date;
    references: AssetReference[];
  }): Asset {
    return new Asset({
      id: props.id,
      version: props.version,
      contentType: props.contentType,
      fileSize: props.fileSize,
      createdAt: props.createdAt,
      references: props.references,
    });
  }
}
