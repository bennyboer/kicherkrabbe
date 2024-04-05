export class Size {
  readonly width: number;
  readonly height: number;

  private constructor(props: { width: number; height: number }) {
    this.width = props.width;
    this.height = props.height;
  }

  static of(props: { width: number; height: number }): Size {
    return new Size(props);
  }

  static zero(): Size {
    return Size.of({ width: 0, height: 0 });
  }

  add(size: Size): Size {
    return Size.of({
      width: this.width + size.width,
      height: this.height + size.height,
    });
  }

  subtract(size: Size): Size {
    return Size.of({
      width: this.width - size.width,
      height: this.height - size.height,
    });
  }

  multiply(factor: number): Size {
    return Size.of({
      width: this.width * factor,
      height: this.height * factor,
    });
  }

  divide(factor: number): Size {
    return Size.of({
      width: this.width / factor,
      height: this.height / factor,
    });
  }

  equals(size: Size): boolean {
    return this.width === size.width && this.height === size.height;
  }
}
