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
import { NotificationService } from '../../../../../shared';
import { environment } from '../../../../../../../environments';

type Mode = 'select' | 'manage';

interface AssetViewModel {
  asset: Asset;
  selected: boolean;
  confirmingDelete: boolean;
  deleting: boolean;
}

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

  protected readonly loading$ = new BehaviorSubject<boolean>(false);
  protected readonly contentTypeItems$: Observable<DropdownItem[]>;
  protected readonly contentTypeFilterArray$: Observable<string[]>;
  protected readonly hasMore$: Observable<boolean>;
  protected readonly assetViewModels$: Observable<AssetViewModel[]>;

  protected readonly sortItems: DropdownItem[] = [
    { id: 'CREATED_AT_DESC', label: 'Datum absteigend' },
    { id: 'CREATED_AT_ASC', label: 'Datum aufsteigend' },
    { id: 'FILE_SIZE_DESC', label: 'Dateigröße absteigend' },
    { id: 'FILE_SIZE_ASC', label: 'Dateigröße aufsteigend' },
  ];

  private readonly assets$ = new BehaviorSubject<Asset[]>([]);
  private readonly searchTerm$ = new BehaviorSubject<string>('');
  private readonly sortProperty$ = new BehaviorSubject<string>('CREATED_AT');
  private readonly sortDirection$ = new BehaviorSubject<string>('DESCENDING');
  private readonly contentTypeFilter$ = new BehaviorSubject<Set<string>>(new Set());
  private readonly availableContentTypes$ = new BehaviorSubject<string[]>([]);
  private readonly total$ = new BehaviorSubject<number>(0);
  private readonly selectedAssetIds$ = new BehaviorSubject<Set<string>>(new Set());
  private readonly confirmingDelete$ = new BehaviorSubject<Set<string>>(new Set());
  private readonly deleting$ = new BehaviorSubject<Set<string>>(new Set());
  private readonly pageSize = 30;
  private readonly destroy$ = new Subject<void>();
  private readonly reload$ = new Subject<void>();

  constructor(
    private readonly assetsService: AssetsService,
    private readonly notificationService: NotificationService,
  ) {
    this.contentTypeItems$ = this.availableContentTypes$.pipe(
      map((types) => types.map((ct) => ({ id: ct, label: ct }))),
    );

    this.contentTypeFilterArray$ = this.contentTypeFilter$.pipe(map((s) => Array.from(s)));

    this.hasMore$ = combineLatest([this.assets$, this.total$]).pipe(
      map(([assets, total]) => assets.length < total),
    );

    this.assetViewModels$ = combineLatest([
      this.assets$,
      this.selectedAssetIds$,
      this.confirmingDelete$,
      this.deleting$,
    ]).pipe(
      map(([assets, selectedIds, confirmingIds, deletingIds]) =>
        assets.map((asset) => ({
          asset,
          selected: selectedIds.has(asset.id),
          confirmingDelete: confirmingIds.has(asset.id),
          deleting: deletingIds.has(asset.id),
        })),
      ),
    );
  }

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
    this.destroy$.next();
    this.destroy$.complete();

    this.assets$.complete();
    this.loading$.complete();
    this.searchTerm$.complete();
    this.sortProperty$.complete();
    this.sortDirection$.complete();
    this.contentTypeFilter$.complete();
    this.availableContentTypes$.complete();
    this.total$.complete();
    this.selectedAssetIds$.complete();
    this.confirmingDelete$.complete();
    this.deleting$.complete();
    this.reload$.complete();
  }

  refresh(): void {
    this.loadContentTypes();
    this.reload$.next();
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
      const hasMore = this.assets$.value.length < this.total$.value;
      if (hasMore && !this.loading$.value) {
        this.loadMore();
      }
    }
  }

  updateSearch(value: string): void {
    this.searchTerm$.next(value.trim());
  }

  onSortChanged(selected: string[]): void {
    if (selected.length === 0) {
      return;
    }
    const value = selected[0];
    const parts = value.split('_');
    const direction = parts.pop()!;
    const property = parts.join('_');
    this.sortProperty$.next(property);
    this.sortDirection$.next(direction === 'ASC' ? 'ASCENDING' : 'DESCENDING');
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

  getSelectedIds(): string[] {
    return Array.from(this.selectedAssetIds$.value);
  }

  requestDeleteAsset(asset: Asset): void {
    if (asset.references.length > 0) {
      return;
    }

    const confirming = new Set(this.confirmingDelete$.value);
    if (!confirming.has(asset.id)) {
      confirming.add(asset.id);
      this.confirmingDelete$.next(confirming);
      return;
    }

    confirming.delete(asset.id);
    this.confirmingDelete$.next(confirming);

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
        this.notificationService.publish({
          type: 'error',
          message: 'Die Datei konnte nicht gelöscht werden.',
        });
      },
    });
  }

  cancelDelete(asset: Asset): void {
    const confirming = new Set(this.confirmingDelete$.value);
    confirming.delete(asset.id);
    this.confirmingDelete$.next(confirming);
  }

  getImageUrl(assetId: string): string {
    return `${environment.apiUrl}/assets/${assetId}/content?width=300`;
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
      .subscribe({
        next: (types) => this.availableContentTypes$.next(types),
        error: () => {
          this.notificationService.publish({
            type: 'error',
            message: 'Die Dateitypen konnten nicht geladen werden.',
          });
        },
      });
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
