import { Point } from '.';
import { Size } from './size';

export class Rect {
  readonly position: Point;
  readonly size: Size;

  private constructor(props: { position: Point; size: Size }) {
    this.position = props.position;
    this.size = props.size;
  }

  static of(props: { position: Point; size: Size }): Rect {
    return new Rect(props);
  }

  static zero(): Rect {
    return Rect.of({ position: Point.zero(), size: Size.zero() });
  }

  getWidth(): number {
    return this.size.width;
  }

  getHeight(): number {
    return this.size.height;
  }

  getXOffset(): number {
    return this.position.x;
  }

  getYOffset(): number {
    return this.position.y;
  }
}
