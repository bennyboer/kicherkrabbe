import { Injectable, OnDestroy } from '@angular/core';
import { filter, map, Observable, Subject, takeUntil } from 'rxjs';
import { SSE } from 'sse.js';
import { HttpClient } from '@angular/common/http';
import { AdminAuthService } from '../../../services';
import { environment } from '../../../../../../environments';
import { Category, CategoryGroup, CategoryId, CLOTHING, NONE } from '../model';
import { none, Option, some, someOrNone } from '../../../../shared/modules/option';

interface CategoryDTO {
  id: string;
  version: number;
  name: string;
  group: CategoryGroupDTO;
  createdAt: string;
}

enum CategoryGroupDTO {
  CLOTHING = 'CLOTHING',
  NONE = 'NONE',
}

interface QueryCategoriesResponse {
  skip: number;
  limit: number;
  total: number;
  categories: CategoryDTO[];
}

interface CreateCategoryRequest {
  name: string;
  group: CategoryGroupDTO;
}

interface RenameCategoryRequest {
  version: number;
  name: string;
}

interface RegroupCategoryRequest {
  version: number;
  group: CategoryGroupDTO;
}

interface CategoryChangeDTO {
  type: string;
  affected: string[];
  payload: any;
}

interface CreateCategoryResponse {
  id: string;
}

@Injectable()
export class CategoriesService implements OnDestroy {
  private readonly events$: Subject<CategoryChangeDTO> = new Subject<CategoryChangeDTO>();
  private readonly destroy$: Subject<void> = new Subject<void>();

  private sse: Option<SSE> = none();

  constructor(
    private readonly http: HttpClient,
    private readonly authService: AdminAuthService,
  ) {
    const loggedOut$ = this.authService.getToken().pipe(filter((token) => token.isNone()));
    loggedOut$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.closeEventStream();
    });
  }

  ngOnDestroy(): void {
    this.events$.complete();

    this.destroy$.next();
    this.destroy$.complete();

    this.closeEventStream();
  }

  getCategoryChanges(): Observable<Set<CategoryId>> {
    this.makeSureEventStreamIsOpen();

    return this.events$.pipe(
      filter((event) => event.type !== 'CREATED'),
      map((event) => new Set<CategoryId>(event.affected)),
    );
  }

  getCategory(id: CategoryId): Observable<Category> {
    return this.http
      .get<CategoryDTO>(`${environment.apiUrl}/categories/${id}`)
      .pipe(map((category) => this.toInternalCategory(category)));
  }

  getCategories(props: {
    group?: CategoryGroup | null;
    searchTerm?: string;
    skip?: number;
    limit?: number;
  }): Observable<Category[]> {
    return someOrNone(props.group)
      .map((group) =>
        this.loadCategoriesByGroup({
          group,
          searchTerm: props.searchTerm,
          skip: props.skip,
          limit: props.limit,
        }),
      )
      .orElseGet(() =>
        this.loadCategories({
          searchTerm: props.searchTerm,
          skip: props.skip,
          limit: props.limit,
        }),
      );
  }

  createCategory(props: { name: string; group: CategoryGroup }): Observable<CategoryId> {
    const request: CreateCategoryRequest = {
      name: props.name,
      group: this.toApiCategoryGroup(props.group),
    };

    return this.http
      .post<CreateCategoryResponse>(`${environment.apiUrl}/categories/create`, request)
      .pipe(map((response) => response.id));
  }

  renameCategory(id: CategoryId, version: number, name: string): Observable<void> {
    const request: RenameCategoryRequest = { version, name };

    return this.http.post<void>(`${environment.apiUrl}/categories/${id}/rename`, request);
  }

  regroupCategory(id: CategoryId, version: number, group: CategoryGroup): Observable<void> {
    const request: RegroupCategoryRequest = {
      version,
      group: this.toApiCategoryGroup(group),
    };

    return this.http.post<void>(`${environment.apiUrl}/categories/${id}/regroup`, request);
  }

  deleteCategory(id: string, version: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/categories/${id}`, {
      params: { version: version.toString() },
    });
  }

  private makeSureEventStreamIsOpen(): void {
    if (this.sse.isNone()) {
      this.openEventStream();
    }
  }

  private openEventStream(): void {
    const token = this.authService.getCurrentToken().orElseThrow();

    const sse = new SSE(`${environment.apiUrl}/categories/changes`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    sse.onmessage = (event) => {
      const change = JSON.parse(event.data);
      this.events$.next(change);
    };
    sse.onabort = () => this.closeEventStream();
    sse.onerror = () => this.closeEventStream();

    this.sse = some(sse);
  }

  private closeEventStream(): void {
    this.sse.ifSome((sse) => sse.close());
    this.sse = none();
  }

  private loadCategories(props: { searchTerm?: string; limit?: number; skip?: number }): Observable<Category[]> {
    const params: any = {};
    someOrNone(props.searchTerm)
      .filter((term) => term.trim().length > 0)
      .ifSome((searchTerm) => (params['searchTerm'] = searchTerm));
    someOrNone(props.limit).ifSome((limit) => (params['limit'] = limit));
    someOrNone(props.skip).ifSome((skip) => (params['skip'] = skip));

    return this.http
      .get<QueryCategoriesResponse>(`${environment.apiUrl}/categories`, {
        params,
      })
      .pipe(map((response) => response.categories.map((category) => this.toInternalCategory(category))));
  }

  private loadCategoriesByGroup(props: {
    group: CategoryGroup;
    searchTerm?: string;
    limit?: number;
    skip?: number;
  }): Observable<Category[]> {
    const categoryGroup: CategoryGroupDTO = this.toApiCategoryGroup(props.group);

    const params: any = {};
    someOrNone(props.searchTerm)
      .filter((term) => term.trim().length > 0)
      .ifSome((searchTerm) => (params['searchTerm'] = searchTerm));
    someOrNone(props.limit).ifSome((limit) => (params['limit'] = limit));
    someOrNone(props.skip).ifSome((skip) => (params['skip'] = skip));

    return this.http
      .get<QueryCategoriesResponse>(`${environment.apiUrl}/categories/groups/${categoryGroup}`, {
        params,
      })
      .pipe(map((response) => response.categories.map((category) => this.toInternalCategory(category))));
  }

  private toInternalCategory(category: CategoryDTO): Category {
    return Category.of({
      id: category.id,
      version: category.version,
      name: category.name,
      group: this.toInternalCategoryGroup(category.group),
      createdAt: new Date(category.createdAt),
    });
  }

  private toInternalCategoryGroup(group: CategoryGroupDTO): CategoryGroup {
    switch (group) {
      case CategoryGroupDTO.CLOTHING:
        return CLOTHING;
      case CategoryGroupDTO.NONE:
        return NONE;
      default:
        throw new Error(`Unknown group type: ${group}`);
    }
  }

  private toApiCategoryGroup(group: CategoryGroup): CategoryGroupDTO {
    switch (group) {
      case CLOTHING:
        return CategoryGroupDTO.CLOTHING;
      case NONE:
        return CategoryGroupDTO.NONE;
      default:
        throw new Error(`Unknown group type: ${group}`);
    }
  }
}
