import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { OfferCategory } from '../model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments';

interface QueryCategoriesResponse {
  categories: OfferCategoryDTO[];
}

interface OfferCategoryDTO {
  id: string;
  name: string;
}

@Injectable()
export class OfferCategoriesService {
  constructor(private readonly http: HttpClient) {}

  getAvailableCategories(): Observable<OfferCategory[]> {
    return this.http.get<QueryCategoriesResponse>(`${environment.apiUrl}/offers/categories`).pipe(
      map((response) =>
        response.categories.map((category) =>
          OfferCategory.of({
            id: category.id,
            name: category.name,
          }),
        ),
      ),
    );
  }
}
