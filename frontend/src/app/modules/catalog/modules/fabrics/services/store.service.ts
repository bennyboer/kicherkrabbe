import { Injectable } from '@angular/core';
import { BehaviorSubject, filter, map, Observable } from 'rxjs';
import { Fabric } from '../model';
import { RemoteFabricsService } from './remote.service';
import { Option } from '../../../../../util';

interface State {
  loaded: boolean;
  loading: boolean;
  fabrics: Fabrics;
}

interface Fabrics {
  [fabricId: string]: Fabric;
}

@Injectable()
export class FabricsStoreService {
  private readonly state$: BehaviorSubject<State> = new BehaviorSubject<State>({
    loaded: false,
    loading: false,
    fabrics: {},
  });

  constructor(private readonly fabricsService: RemoteFabricsService) {
    this.reloadFabrics();
  }

  getFabrics(): Observable<Fabric[]> {
    return this.getState().pipe(
      filter((state) => state.loaded),
      map((state) => Object.values(state.fabrics)),
    );
  }

  getFabricById(id: string): Observable<Fabric> {
    return this.getState().pipe(
      map((state) => Option.someOrNone(state.fabrics[id])),
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

  private reloadFabrics(): void {
    this.updateState((state) => ({ ...state, loading: true }));

    this.fabricsService.getFabrics().subscribe((fabrics) => {
      const fabricsMap = fabrics.reduce(
        (acc, fabric) => ({ ...acc, [fabric.id]: fabric }),
        {},
      );

      this.updateState((state) => ({
        ...state,
        loaded: true,
        loading: false,
        fabrics: fabricsMap,
      }));
    });
  }
}
