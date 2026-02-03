import { someOrNone } from '@kicherkrabbe/shared';

interface Props {
  [key: string]: unknown;
}

export const validateProps = (props?: Props) => {
  return someOrNone(props)
    .map((p) => {
      for (const entry of Object.entries(p)) {
        const key = entry[0];
        const value = entry[1];

        if (someOrNone(value).isNone()) {
          throw Error(`Property ${key} is required`);
        }
      }

      return props;
    })
    .orElseThrow('Properties are required');
};
