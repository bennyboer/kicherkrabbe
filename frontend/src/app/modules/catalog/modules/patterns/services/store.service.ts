import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, filter, map, Observable } from 'rxjs';
import { Pattern } from '../model';
import { Option } from '../../../../../util';
import { RemotePatternsService } from './remote.service';

interface State {
  loaded: boolean;
  loading: boolean;
  patterns: Patterns;
}

interface Patterns {
  [patternId: string]: Pattern;
}

@Injectable()
export class PatternsStoreService implements OnDestroy {
  private readonly state$: BehaviorSubject<State> = new BehaviorSubject<State>({
    loaded: false,
    loading: false,
    patterns: {},
  });

  constructor(private readonly patternsService: RemotePatternsService) {
    this.reloadPatterns();
  }

  ngOnDestroy(): void {
    this.state$.complete();
  }

  getPatterns(): Observable<Pattern[]> {
    return this.getState().pipe(
      filter((state) => state.loaded),
      map((state) => Object.values(state.patterns)),
    );
  }

  getPatternById(id: string): Observable<Pattern> {
    return this.getState().pipe(
      map((state) => Option.someOrNone(state.patterns[id])),
      filter((pattern) => pattern.isSome()),
      map((pattern) => pattern.orElseThrow()),
    );
  }

  private getState(): Observable<State> {
    return this.state$.asObservable();
  }

  private updateState(updater: (state: State) => State): void {
    this.state$.next(updater(this.state$.value));
  }

  private reloadPatterns(): void {
    this.updateState((state) => ({ ...state, loading: true }));

    this.patternsService.getPatterns().subscribe((patterns) => {
      const patternsMap = patterns.reduce(
        (acc, pattern) => ({ ...acc, [pattern.id]: pattern }),
        {},
      );

      this.updateState((state) => ({
        ...state,
        loaded: true,
        loading: false,
        patterns: patternsMap,
      }));
    });
  }
}
