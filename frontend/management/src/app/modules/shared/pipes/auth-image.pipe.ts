import { OnDestroy, Pipe, PipeTransform } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable, of, shareReplay } from 'rxjs';

@Pipe({
  name: 'authImage',
  standalone: true,
})
export class AuthImagePipe implements PipeTransform, OnDestroy {
  private objectUrl: string | null = null;
  private previousUrl: string | null = null;
  private result$: Observable<string> | null = null;

  constructor(private readonly http: HttpClient) {}

  transform(url: string | null): Observable<string> {
    if (!url) {
      return of('');
    }

    if (url === this.previousUrl && this.result$) {
      return this.result$;
    }

    this.previousUrl = url;
    this.revokeObjectUrl();

    this.result$ = this.http.get(url, { responseType: 'blob' }).pipe(
      map((blob) => {
        this.revokeObjectUrl();
        this.objectUrl = URL.createObjectURL(blob);
        return this.objectUrl;
      }),
      shareReplay(1),
    );

    return this.result$;
  }

  ngOnDestroy(): void {
    this.revokeObjectUrl();
  }

  private revokeObjectUrl(): void {
    if (this.objectUrl) {
      URL.revokeObjectURL(this.objectUrl);
      this.objectUrl = null;
    }
  }
}
