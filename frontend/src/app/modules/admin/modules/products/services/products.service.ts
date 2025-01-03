import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ProductsService {
  constructor(private readonly http: HttpClient) {}
}
