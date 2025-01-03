import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable } from 'rxjs';
import { FabricsService } from '../../services';
import { Fabric } from '../../model';
import { environment } from '../../../../../../../environments';

@Component({
    selector: 'app-fabrics-page',
    templateUrl: './fabrics.page.html',
    styleUrls: ['./fabrics.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class FabricsPage implements OnDestroy {
  private readonly search$: BehaviorSubject<string> = new BehaviorSubject<string>('');

  constructor(private readonly fabricsService: FabricsService) {}

  ngOnDestroy(): void {
    this.search$.complete();
  }

  getFabrics(): Observable<Fabric[]> {
    return combineLatest([this.fabricsService.getFabrics(), this.search$]).pipe(
      map(([fabrics, search]) => fabrics.filter((fabric) => fabric.name.toLowerCase().includes(search.toLowerCase()))),
    );
  }

  isSearching(): Observable<boolean> {
    return this.search$.pipe(map((search) => search.length > 0));
  }

  isLoading(): Observable<boolean> {
    return this.fabricsService.isLoading();
  }

  isFailed(): Observable<boolean> {
    return this.fabricsService.isFailedLoadingFabrics();
  }

  updateSearch(value: string): void {
    this.search$.next(value.trim());
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }
}
