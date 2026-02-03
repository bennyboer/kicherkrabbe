import { Pipe, PipeTransform } from '@angular/core';
import { Option, someOrNone } from '@kicherkrabbe/shared';

@Pipe({
  name: 'ifSome',
  standalone: false,
})
export class IfSomePipe implements PipeTransform {
  transform<T>(value?: Option<T> | null): T | null | undefined {
    return someOrNone(value)
      .flatMap((o) => o)
      .orElseNull();
  }
}
