import { someOrNone } from './option';

export class Image {
  readonly url: string;

  private constructor(props: { url: string }) {
    this.url = someOrNone(props.url).orElseThrow('Image URL is required');
  }

  static of(props: { url: string }): Image {
    return new Image({
      url: props.url,
    });
  }
}
