import { Injectable } from '@angular/core';
import {
  BehaviorSubject,
  distinctUntilChanged,
  filter,
  map,
  Observable,
} from 'rxjs';
import { Color, Fabric, Theme } from '../model';
import { RemoteFabricsService } from './remote.service';
import { none, Option, some, someOrNone } from '../../../../../util';

export class FabricsFilter {
  readonly colorIds: Option<Set<string>>;
  readonly themeIds: Option<Set<string>>;
  readonly inStock: Option<boolean>;

  private constructor(props: {
    colorIds: Option<Set<string>>;
    themeIds: Option<Set<string>>;
    inStock: Option<boolean>;
  }) {
    this.colorIds = props.colorIds;
    this.themeIds = props.themeIds;
    this.inStock = props.inStock;
  }

  static of(props: {
    colorIds: Option<Set<string>>;
    themeIds: Option<Set<string>>;
    inStock: Option<boolean>;
  }): FabricsFilter {
    return new FabricsFilter({
      colorIds: props.colorIds,
      themeIds: props.themeIds,
      inStock: props.inStock,
    });
  }

  equals(other: FabricsFilter): boolean {
    return (
      this.colorIds.equals(other.colorIds) &&
      this.themeIds.equals(other.themeIds) &&
      this.inStock.equals(other.inStock)
    );
  }
}

interface State {
  loaded: boolean;
  loading: boolean;
  fabrics: Fabrics;
  filter: Option<FabricsFilter>;
  availableColors: Set<Color>;
  availableThemes: Set<Theme>;
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
    filter: none(),
    availableColors: new Set<Color>(),
    availableThemes: new Set<Theme>(),
  });

  constructor(private readonly fabricsService: RemoteFabricsService) {
    this.reloadFabrics();
    this.reloadAvailableTopics();
    this.reloadAvailableColors();
  }

  getAvailableColors(): Observable<Color[]> {
    return this.getState().pipe(
      filter((state) => state.availableColors.size > 0),
      map((state) => Array.from(state.availableColors)),
      distinctUntilChanged(),
    );
  }

  getAvailableThemes(): Observable<Theme[]> {
    return this.getState().pipe(
      filter((state) => state.availableThemes.size > 0),
      map((state) => Array.from(state.availableThemes)),
      distinctUntilChanged(),
    );
  }

  getFabrics(): Observable<Fabric[]> {
    return this.getState().pipe(
      filter((state) => state.loaded),
      map((state) => Object.values(state.fabrics)),
    );
  }

  getFabricById(id: string): Observable<Fabric> {
    return this.getState().pipe(
      map((state) => someOrNone(state.fabrics[id])),
      filter((pattern) => pattern.isSome()),
      map((pattern) => pattern.orElseThrow()),
    );
  }

  updateFilter(filter: FabricsFilter): void {
    const oldFilter = this.state$.value.filter;
    if (oldFilter.isSome() && oldFilter.orElseThrow().equals(filter)) {
      return;
    }

    this.updateState((state) => ({ ...state, filter: some(filter) }));
    this.reloadFabrics();
  }

  private getState(): Observable<State> {
    return this.state$.asObservable();
  }

  private updateState(updater: (state: State) => State): void {
    this.state$.next(updater(this.state$.value));
  }

  private reloadFabrics(): void {
    this.updateState((state) => ({ ...state, loading: true }));

    const filter = this.state$.value.filter;
    const topicIds = filter
      .map((filter) => Array.from(filter.themeIds.orElse(new Set())))
      .orElse([]);
    const colorIds = filter
      .map((filter) => Array.from(filter.colorIds.orElse(new Set())))
      .orElse([]);
    const availability = filter
      .flatMap((filter) => filter.inStock)
      .map((inStock) => ({ active: true, inStock }))
      .orElse({ active: false, inStock: true });

    this.fabricsService
      .getFabrics({
        topicIds,
        colorIds,
        availability,
      })
      .subscribe((fabrics) => {
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

  private reloadAvailableTopics(): void {
    this.fabricsService.getAvailableThemes().subscribe((themes) => {
      this.updateState((state) => ({
        ...state,
        availableThemes: new Set<Theme>(themes),
      }));
    });
  }

  private reloadAvailableColors(): void {
    this.fabricsService.getAvailableColors().subscribe((colors) => {
      this.updateState((state) => ({
        ...state,
        availableColors: new Set<Color>(colors),
      }));
    });
  }
}
