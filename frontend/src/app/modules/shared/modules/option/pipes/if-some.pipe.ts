import { Pipe, PipeTransform } from '@angular/core';
import { Option, someOrNone } from '../option';

@Pipe({
  name: 'ifSome',
})
export class IfSomePipe implements PipeTransform {
  transform<T>(value?: Option<T> | null): T | null | undefined {
    return someOrNone(value)
      .flatMap((o) => o)
      .orElseNull();
  }
}
