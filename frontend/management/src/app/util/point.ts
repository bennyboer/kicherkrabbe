export class Point {
  readonly x: number;
  readonly y: number;

  private constructor(props: { x: number; y: number }) {
    this.x = props.x;
    this.y = props.y;
  }

  static of(props: { x: number; y: number }): Point {
    return new Point({ x: props.x, y: props.y });
  }

  static zero(): Point {
    return new Point({ x: 0, y: 0 });
  }

  add(point: Point): Point {
    return Point.of({
      x: this.x + point.x,
      y: this.y + point.y,
    });
  }

  subtract(point: Point): Point {
    return Point.of({
      x: this.x - point.x,
      y: this.y - point.y,
    });
  }

  scale(factor: number): Point {
    return Point.of({
      x: this.x * factor,
      y: this.y * factor,
    });
  }

  equals(point: Point): boolean {
    return this.x === point.x && this.y === point.y;
  }
}
