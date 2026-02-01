import { validateProps } from '../../../../../util';

export enum InternalLinkType {
  PATTERN = 'PATTERN',
  FABRIC = 'FABRIC',
}

export class LinkType {
  readonly internal: InternalLinkType;
  readonly label: string;

  private constructor(props: { internal: InternalLinkType; label: string }) {
    validateProps(props);

    this.internal = props.internal;
    this.label = props.label;
  }

  static pattern(): LinkType {
    return new LinkType({ internal: InternalLinkType.PATTERN, label: 'Schnitt' });
  }

  static fabric(): LinkType {
    return new LinkType({ internal: InternalLinkType.FABRIC, label: 'Stoff' });
  }
}

export const PATTERN = LinkType.pattern();
export const FABRIC = LinkType.fabric();

export const LINK_TYPES = [PATTERN, FABRIC];
