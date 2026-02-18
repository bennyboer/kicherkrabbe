import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../../../environments';

type AssetId = string;

interface UploadAssetResponse {
  assetId: string;
}

export interface AssetReferenceDTO {
  resourceType: string;
  resourceId: string;
  resourceName: string;
}

export interface AssetDTO {
  id: string;
  version: number;
  contentType: string;
  fileSize: number;
  createdAt: string;
  references: AssetReferenceDTO[];
}

export interface QueryAssetsRequest {
  searchTerm: string;
  contentTypes: string[];
  sortProperty: string;
  sortDirection: string;
  skip: number;
  limit: number;
}

export interface QueryAssetsResponse {
  skip: number;
  limit: number;
  total: number;
  assets: AssetDTO[];
}

interface QueryContentTypesResponse {
  contentTypes: string[];
}

export interface StorageInfoResponse {
  usedBytes: number;
  limitBytes: number;
}

@Injectable()
export class AssetsService {
  constructor(private readonly http: HttpClient) {}

  uploadAsset(blob: Blob): Observable<AssetId> {
    const formData = new FormData();
    formData.append('content', blob);

    const headers = new HttpHeaders({ enctype: 'multipart/form-data' });

    return this.http
      .post<UploadAssetResponse>(`${environment.apiUrl}/assets/upload`, formData, {
        headers: headers,
      })
      .pipe(map((response) => response.assetId));
  }

  queryAssets(request: QueryAssetsRequest): Observable<QueryAssetsResponse> {
    return this.http.post<QueryAssetsResponse>(`${environment.apiUrl}/assets/`, request);
  }

  getContentTypes(): Observable<string[]> {
    return this.http
      .get<QueryContentTypesResponse>(`${environment.apiUrl}/assets/content-types`)
      .pipe(map((response) => response.contentTypes));
  }

  getStorageInfo(): Observable<StorageInfoResponse> {
    return this.http.get<StorageInfoResponse>(`${environment.apiUrl}/assets/storage-info`);
  }

  deleteAsset(id: string, version: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/assets/${id}/`, {
      params: { version: version.toString() },
    });
  }
}
