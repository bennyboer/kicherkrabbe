import { Pipe, PipeTransform } from '@angular/core';
import { Option, someOrNone } from '@kicherkrabbe/shared';

@Pipe({
  name: 'ifNone',
  standalone: false,
})
export class IfNonePipe implements PipeTransform {
  transform<T>(value?: Option<T>): boolean {
    return someOrNone(value)
      .flatMap((o) => o)
      .map(() => false)
      .orElse(true);
  }
}
