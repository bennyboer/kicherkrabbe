import { ChangeDetectionStrategy, Component } from '@angular/core';
import { map, Observable, switchMap } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { FabricsStoreService } from '../../services';
import { Fabric } from '../../model';

@Component({
  selector: 'app-fabric-page',
  templateUrl: './fabric.page.html',
  styleUrls: ['./fabric.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FabricPage {
  protected readonly fabric$: Observable<Fabric> = this.route.params.pipe(
    map((params) => params['id']),
    switchMap((id) => this.fabricsStore.getFabricById(id)),
  );

  constructor(
    private readonly fabricsStore: FabricsStoreService,
    private readonly route: ActivatedRoute,
  ) {}
}
