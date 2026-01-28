import { ChangeDetectionStrategy, Component } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable } from 'rxjs';
import { FabricTypesService } from '../../services';
import { FabricType } from '../../model';

@Component({
  selector: 'app-fabric-types-page',
  templateUrl: './fabric-types.page.html',
  styleUrls: ['./fabric-types.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class FabricTypesPage {
  private readonly search$: BehaviorSubject<string> = new BehaviorSubject<string>('');

  constructor(private readonly fabricTypesService: FabricTypesService) {}

  ngOnDestroy(): void {
    this.search$.complete();
  }

  getFabricTypes(): Observable<FabricType[]> {
    return combineLatest([this.fabricTypesService.getFabricTypes(), this.search$]).pipe(
      map(([fabricTypes, search]) =>
        fabricTypes.filter((fabricType) => fabricType.name.toLowerCase().includes(search.toLowerCase())),
      ),
    );
  }

  isSearching(): Observable<boolean> {
    return this.search$.pipe(map((search) => search.length > 0));
  }

  isLoading(): Observable<boolean> {
    return this.fabricTypesService.isLoading();
  }

  isFailed(): Observable<boolean> {
    return this.fabricTypesService.isFailedLoadingFabricTypes();
  }

  updateSearch(value: string): void {
    this.search$.next(value.trim());
  }
}
