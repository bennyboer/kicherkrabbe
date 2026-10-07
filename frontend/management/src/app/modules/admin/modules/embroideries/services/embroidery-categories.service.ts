import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { EmbroideryCategory } from '../model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments';

interface QueryCategoriesResponse {
  categories: EmbroideryCategoryDTO[];
}

interface EmbroideryCategoryDTO {
  id: string;
  name: string;
}

@Injectable()
export class EmbroideryCategoriesService {
  constructor(private readonly http: HttpClient) {}

  getAvailableCategories(): Observable<EmbroideryCategory[]> {
    return this.http
      .get<QueryCategoriesResponse>(`${environment.apiUrl}/embroideries/categories`)
      .pipe(map((response) => response.categories.map((category) => this.toInternalCategory(category))));
  }

  getUsedCategories(): Observable<EmbroideryCategory[]> {
    return this.http
      .get<QueryCategoriesResponse>(`${environment.apiUrl}/embroideries/categories/used`)
      .pipe(map((response) => response.categories.map((category) => this.toInternalCategory(category))));
  }

  private toInternalCategory(category: EmbroideryCategoryDTO): EmbroideryCategory {
    return EmbroideryCategory.of({
      id: category.id,
      name: category.name,
    });
  }
}
