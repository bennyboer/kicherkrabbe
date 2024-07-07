import { ImageSliderImage } from './image';
import { someOrNone } from '../../../../../util';

export class Thumbnail {
  readonly image: ImageSliderImage;
  readonly active: boolean;

  private constructor(props: { image: ImageSliderImage; active: boolean }) {
    if (!props.image) {
      throw new Error('Image is required');
    }

    this.image = props.image;
    this.active = props.active;
  }

  static of(props: { image: ImageSliderImage; active?: boolean }): Thumbnail {
    return new Thumbnail({
      image: props.image,
      active: someOrNone(props.active).orElse(false),
    });
  }
}
