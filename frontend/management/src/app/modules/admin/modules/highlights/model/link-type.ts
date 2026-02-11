export enum LinkType {
  PATTERN = 'PATTERN',
  FABRIC = 'FABRIC',
}

export function toLinkType(value: string): LinkType {
  if (Object.values(LinkType).includes(value as LinkType)) {
    return value as LinkType;
  }
  throw new Error(`Unknown link type: ${value}`);
}
