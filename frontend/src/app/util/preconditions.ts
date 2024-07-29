import { Option } from './option';

interface Props {
  [key: string]: unknown;
}

export const validateProps = (props?: Props) => {
  return Option.someOrNone(props)
    .map((p) => {
      for (const entry of Object.entries(p)) {
        const key = entry[0];
        const value = entry[1];

        if (Option.someOrNone(value).isNone()) {
          throw Error(`Property ${key} is required`);
        }
      }

      return props;
    })
    .orElseThrow('Properties are required');
};
