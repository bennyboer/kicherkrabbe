import { Injectable } from '@angular/core';
import { Meta } from '@angular/platform-browser';
import { someOrNone } from '../util';

@Injectable()
export class RoutingMetadataService {
  constructor(private readonly meta: Meta) {}

  updateDescription(description?: string) {
    this.meta.updateTag({
      name: 'description',
      content: someOrNone(description).orElse(
        'Handmade Kleidung für Kinder und Babys aus Bayern',
      ),
    });
  }
}
