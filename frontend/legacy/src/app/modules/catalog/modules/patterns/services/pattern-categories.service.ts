import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { Category } from '../model';
import { environment } from '../../../../../../environments';

interface QueryCategoriesResponse {
  categories: CategoryDTO[];
}

interface CategoryDTO {
  id: string;
  name: string;
}

@Injectable()
export class PatternCategoriesService {
  constructor(private readonly http: HttpClient) {}

  getCategories(): Observable<Category[]> {
    return this.http.get<QueryCategoriesResponse>(`${environment.apiUrl}/patterns/categories/used`).pipe(
      map((response) =>
        response.categories.map((category) =>
          Category.of({
            id: category.id,
            name: category.name,
          }),
        ),
      ),
    );
  }
}
