import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { PatternCategory } from '../model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments';

interface QueryCategoriesResponse {
  categories: PatternCategoryDTO[];
}

interface PatternCategoryDTO {
  id: string;
  name: string;
}

@Injectable()
export class PatternCategoriesService {
  constructor(private readonly http: HttpClient) {}

  getCategories(): Observable<PatternCategory[]> {
    return this.http
      .get<QueryCategoriesResponse>(`${environment.apiUrl}/patterns/categories`)
      .pipe(
        map((response) =>
          response.categories.map((category) =>
            PatternCategory.of({
              id: category.id,
              name: category.name,
            }),
          ),
        ),
      );
  }
}
