import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../../../environments';

type AssetId = string;

interface UploadAssetResponse {
  assetId: string;
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
}
