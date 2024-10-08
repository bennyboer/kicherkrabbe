import { Injectable } from '@angular/core';
import { environment } from '../../../../environments';
import { someOrNone } from '../modules/option';

export enum Flag {
  TEST = 'TEST',
}

@Injectable()
export class FlagService {
  private readonly activeFlags = new Map<Flag, boolean>();

  constructor() {
    const activeFlags = environment.flags;
    for (const flagName of activeFlags) {
      const flag = Flag[flagName as keyof typeof Flag];
      this.activeFlags.set(flag, true);
    }
  }

  isActive(flag: Flag): boolean {
    return someOrNone(this.activeFlags.get(flag)).orElse(false);
  }
}
