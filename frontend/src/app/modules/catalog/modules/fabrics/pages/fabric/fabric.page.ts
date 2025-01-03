import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, map, Observable, Subject, switchMap, takeUntil } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { RemoteFabricsService } from '../../services';
import { Fabric, Type } from '../../model';

@Component({
    selector: 'app-fabric-page',
    templateUrl: './fabric.page.html',
    styleUrls: ['./fabric.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class FabricPage implements OnInit, OnDestroy {
  private readonly fabricTypes$: BehaviorSubject<Type[]> = new BehaviorSubject<Type[]>([]);
  private readonly destroy$: Subject<void> = new Subject<void>();

  protected readonly fabric$: Observable<Fabric> = this.route.params.pipe(
    map((params) => params['id']),
    switchMap((id) => this.fabricsService.getFabric(id)),
  );
  protected readonly fabricTypeLabels$: Observable<Map<string, string>> = this.fabricTypes$.pipe(
    map((types) => types.reduce((acc, type) => acc.set(type.id, type.name), new Map())),
  );

  constructor(
    private readonly fabricsService: RemoteFabricsService,
    private readonly route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.fabricsService
      .getAvailableFabricTypes()
      .pipe(takeUntil(this.destroy$))
      .subscribe((types) => this.fabricTypes$.next(types));
  }

  ngOnDestroy(): void {
    this.fabricTypes$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }
}
