import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import {
  BehaviorSubject,
  combineLatest,
  debounceTime,
  distinctUntilChanged,
  map,
  Observable,
  Subject,
  takeUntil,
} from 'rxjs';
import { AssetsService, AssetDTO } from '../../services/assets.service';
import { Asset, AssetReference } from '../../model';
import { DropdownItem } from '../../../../../shared/components/dropdown/dropdown.component';
import { environment } from '../../../../../../../environments';

type Mode = 'select' | 'manage';

@Component({
  selector: 'app-asset-browser',
  templateUrl: './asset-browser.component.html',
  styleUrls: ['./asset-browser.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class AssetBrowserComponent implements OnInit, OnDestroy {
  @ViewChild('scrollContainer')
  scrollContainer!: ElementRef;

  @Input()
  mode: Mode = 'manage';

  @Input()
  multiple: boolean = false;

  @Input()
  set initialContentTypes(types: string[]) {
    if (types && types.length > 0) {
      this.contentTypeFilter$.next(new Set(types));
    }
  }

  @Output()
  assetSelected = new EventEmitter<string>();

  @Output()
  assetsSelected = new EventEmitter<string[]>();

  protected readonly assets$ = new BehaviorSubject<Asset[]>([]);
  protected readonly loading$ = new BehaviorSubject<boolean>(false);
  protected readonly searchTerm$ = new BehaviorSubject<string>('');
  protected readonly sortProperty$ = new BehaviorSubject<string>('CREATED_AT');
  protected readonly sortDirection$ = new BehaviorSubject<string>('DESCENDING');
  protected readonly contentTypeFilter$ = new BehaviorSubject<Set<string>>(new Set());
  protected readonly availableContentTypes$ = new BehaviorSubject<string[]>([]);
  protected readonly total$ = new BehaviorSubject<number>(0);
  protected readonly selectedAssetIds$ = new BehaviorSubject<Set<string>>(new Set());
  protected readonly deleting$ = new BehaviorSubject<Set<string>>(new Set());

  protected readonly sortItems: DropdownItem[] = [
    { id: 'CREATED_AT_DESC', label: 'Datum absteigend' },
    { id: 'CREATED_AT_ASC', label: 'Datum aufsteigend' },
    { id: 'FILE_SIZE_DESC', label: 'Dateigröße absteigend' },
    { id: 'FILE_SIZE_ASC', label: 'Dateigröße aufsteigend' },
  ];

  private readonly pageSize = 30;
  private readonly destroy$ = new Subject<void>();
  private readonly reload$ = new Subject<void>();

  constructor(private readonly assetsService: AssetsService) {}

  ngOnInit(): void {
    this.loadContentTypes();

    combineLatest([
      this.searchTerm$.pipe(debounceTime(300), distinctUntilChanged()),
      this.sortProperty$,
      this.sortDirection$,
      this.contentTypeFilter$.pipe(
        distinctUntilChanged((a, b) => a.size === b.size && [...a].every((v) => b.has(v))),
      ),
      this.reload$,
    ])
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.assets$.next([]);
        this.total$.next(0);
        this.loadPage(0);
      });

    this.reload$.next();
  }

  ngOnDestroy(): void {
    this.assets$.complete();
    this.loading$.complete();
    this.searchTerm$.complete();
    this.sortProperty$.complete();
    this.sortDirection$.complete();
    this.contentTypeFilter$.complete();
    this.availableContentTypes$.complete();
    this.total$.complete();
    this.selectedAssetIds$.complete();
    this.deleting$.complete();

    this.destroy$.next();
    this.destroy$.complete();
    this.reload$.complete();
  }

  refresh(): void {
    this.loadContentTypes();
    this.reload$.next();
  }

  hasMore(): Observable<boolean> {
    return combineLatest([this.assets$, this.total$]).pipe(map(([assets, total]) => assets.length < total));
  }

  loadMore(): void {
    if (this.loading$.value) {
      return;
    }
    this.loadPage(this.assets$.value.length);
  }

  onScroll(event: Event): void {
    const el = event.target as HTMLElement;
    const threshold = 200;
    if (el.scrollHeight - el.scrollTop - el.clientHeight < threshold) {
      combineLatest([this.hasMore(), this.loading$])
        .pipe(
          map(([hasMore, loading]) => hasMore && !loading),
        )
        .subscribe((shouldLoad) => {
          if (shouldLoad) {
            this.loadMore();
          }
        })
        .unsubscribe();
    }
  }

  updateSearch(value: string): void {
    this.searchTerm$.next(value.trim());
  }

  updateSort(property: string, direction: string): void {
    this.sortProperty$.next(property);
    this.sortDirection$.next(direction);
  }

  toggleContentTypeFilter(contentType: string): void {
    const current = new Set(this.contentTypeFilter$.value);
    if (current.has(contentType)) {
      current.delete(contentType);
    } else {
      current.add(contentType);
    }
    this.contentTypeFilter$.next(current);
  }

  clearContentTypeFilter(): void {
    this.contentTypeFilter$.next(new Set());
  }

  toContentTypeItems(contentTypes: string[]): DropdownItem[] {
    return contentTypes.map((ct) => ({ id: ct, label: ct }));
  }

  getContentTypeFilterArray(): Observable<string[]> {
    return this.contentTypeFilter$.pipe(map((s) => Array.from(s)));
  }

  onSortChanged(selected: string[]): void {
    if (selected.length === 0) {
      return;
    }
    const value = selected[0];
    const parts = value.split('_');
    const direction = parts.pop()!;
    const property = parts.join('_');
    this.updateSort(property, direction === 'ASC' ? 'ASCENDING' : 'DESCENDING');
  }

  onContentTypeFilterChanged(selected: string[]): void {
    this.contentTypeFilter$.next(new Set(selected));
  }

  onAssetClick(asset: Asset): void {
    if (this.mode === 'select') {
      if (this.multiple) {
        const current = new Set(this.selectedAssetIds$.value);
        if (current.has(asset.id)) {
          current.delete(asset.id);
        } else {
          current.add(asset.id);
        }
        this.selectedAssetIds$.next(current);
        this.assetsSelected.emit(Array.from(current));
      } else {
        this.selectedAssetIds$.next(new Set([asset.id]));
        this.assetSelected.emit(asset.id);
      }
    }
  }

  isSelected(assetId: string): Observable<boolean> {
    return this.selectedAssetIds$.pipe(map((selected) => selected.has(assetId)));
  }

  getSelectedIds(): string[] {
    return Array.from(this.selectedAssetIds$.value);
  }

  deleteAsset(asset: Asset): void {
    if (asset.references.length > 0) {
      return;
    }

    const current = new Set(this.deleting$.value);
    current.add(asset.id);
    this.deleting$.next(current);

    this.assetsService.deleteAsset(asset.id, asset.version).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        const updated = new Set(this.deleting$.value);
        updated.delete(asset.id);
        this.deleting$.next(updated);
        this.refresh();
      },
      error: () => {
        const updated = new Set(this.deleting$.value);
        updated.delete(asset.id);
        this.deleting$.next(updated);
      },
    });
  }

  isDeleting(assetId: string): Observable<boolean> {
    return this.deleting$.pipe(map((deleting) => deleting.has(assetId)));
  }

  getImageUrl(assetId: string): string {
    return `${environment.apiUrl}/assets/${assetId}/content`;
  }

  getReferenceSummary(asset: Asset): string {
    if (asset.references.length === 0) {
      return 'Nicht verwendet';
    }

    return asset.references
      .map((ref) => {
        const name = ref.resourceName || ref.resourceId;
        return `${this.translateResourceType(ref.resourceType)}: ${name}`;
      })
      .join(', ');
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) {
      return `${bytes} B`;
    }
    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} KB`;
    }
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  getSortLabel(): Observable<string> {
    return combineLatest([this.sortProperty$, this.sortDirection$]).pipe(
      map(([prop, dir]) => {
        const propLabel = prop === 'CREATED_AT' ? 'Datum' : 'Dateigröße';
        const dirLabel = dir === 'ASCENDING' ? 'aufsteigend' : 'absteigend';
        return `${propLabel} ${dirLabel}`;
      }),
    );
  }

  private translateResourceType(type: string): string {
    switch (type) {
      case 'FABRIC':
        return 'Stoff';
      case 'PATTERN':
        return 'Schnittmuster';
      case 'PRODUCT':
        return 'Produkt';
      case 'HIGHLIGHT':
        return 'Highlight';
      default:
        return type;
    }
  }

  private loadContentTypes(): void {
    this.assetsService
      .getContentTypes()
      .pipe(takeUntil(this.destroy$))
      .subscribe((types) => this.availableContentTypes$.next(types));
  }

  private loadPage(skip: number): void {
    this.loading$.next(true);

    const contentTypes = Array.from(this.contentTypeFilter$.value);

    this.assetsService
      .queryAssets({
        searchTerm: this.searchTerm$.value,
        contentTypes,
        sortProperty: this.sortProperty$.value,
        sortDirection: this.sortDirection$.value,
        skip,
        limit: this.pageSize,
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          const newAssets = response.assets.map((dto) => this.mapAsset(dto));

          if (skip === 0) {
            this.assets$.next(newAssets);
          } else {
            this.assets$.next([...this.assets$.value, ...newAssets]);
          }

          this.total$.next(response.total);
          this.loading$.next(false);
        },
        error: () => {
          this.loading$.next(false);
        },
      });
  }

  private mapAsset(dto: AssetDTO): Asset {
    return Asset.of({
      id: dto.id,
      version: dto.version,
      contentType: dto.contentType,
      fileSize: dto.fileSize,
      createdAt: new Date(dto.createdAt),
      references: dto.references.map((ref) =>
        AssetReference.of({
          resourceType: ref.resourceType,
          resourceId: ref.resourceId,
          resourceName: ref.resourceName,
        }),
      ),
    });
  }
}
