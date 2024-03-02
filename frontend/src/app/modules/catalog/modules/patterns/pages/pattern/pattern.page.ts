import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PatternsStoreService } from '../../services';
import { map, switchMap } from 'rxjs';

@Component({
  selector: 'app-pattern-page',
  templateUrl: './pattern.page.html',
  styleUrls: ['./pattern.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternPage {
  protected readonly pattern$ = this.route.params.pipe(
    map((params) => params['id']),
    switchMap((id) => this.patternsStore.getPatternById(id)),
  );

  constructor(
    private readonly patternsStore: PatternsStoreService,
    private readonly route: ActivatedRoute,
  ) {}
}
