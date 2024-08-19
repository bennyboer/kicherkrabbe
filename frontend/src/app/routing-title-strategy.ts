import { Injectable } from '@angular/core';
import { RouterStateSnapshot, TitleStrategy } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { someOrNone } from './util';

@Injectable()
export class RoutingTitleStrategy extends TitleStrategy {
  constructor(private readonly title: Title) {
    super();
  }

  updateTitle(snapshot: RouterStateSnapshot): void {
    someOrNone(this.buildTitle(snapshot)).ifSomeOrElse(
      (title) => this.title.setTitle(`Kicherkrabbe | ${title}`),
      () => this.title.setTitle('Kicherkrabbe | Handmade Kinderkleidung'),
    );
  }
}
