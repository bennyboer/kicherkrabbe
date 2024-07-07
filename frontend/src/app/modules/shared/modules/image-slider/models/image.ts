export class ImageSliderImage {
  readonly url: string;

  private constructor(props: { url: string }) {
    if (!props.url) {
      throw new Error('Image url is required');
    }

    this.url = props.url;
  }

  static of(props: { url: string }): ImageSliderImage {
    return new ImageSliderImage({
      url: props.url,
    });
  }
}
