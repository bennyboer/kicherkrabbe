import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { BotSettings, Settings } from '../model';
import { environment } from '../../../../../../environments';

interface QuerySettingsResponse {
  settings: SettingsDTO;
}

interface SettingsDTO {
  version: number;
  botSettings: BotSettingsDTO;
}

interface BotSettingsDTO {
  maskedApiToken: string;
}

interface UpdateBotApiTokenRequest {
  version: number;
  apiToken: string;
}

interface UpdateBotApiTokenResponse {
  version: number;
}

interface ClearBotApiTokenRequest {
  version: number;
}

interface ClearBotApiTokenResponse {
  version: number;
}

@Injectable()
export class TelegramService {
  constructor(private readonly http: HttpClient) {}

  getSettings(): Observable<Settings> {
    return this.http.get<QuerySettingsResponse>(`${environment.apiUrl}/telegram/settings`).pipe(
      map((response) => {
        return Settings.of({
          version: response.settings.version,
          botSettings: BotSettings.of({
            apiToken: response.settings.botSettings.maskedApiToken,
          }),
        });
      }),
    );
  }

  updateBotApiToken(version: number, newToken: string): Observable<number> {
    const request: UpdateBotApiTokenRequest = {
      version,
      apiToken: newToken,
    };

    return this.http
      .post<UpdateBotApiTokenResponse>(`${environment.apiUrl}/telegram/settings/bot/api-token/update`, request)
      .pipe(map((response) => response.version));
  }

  clearBotApiToken(version: number): Observable<number> {
    const request: ClearBotApiTokenRequest = {
      version,
    };

    return this.http
      .post<ClearBotApiTokenResponse>(`${environment.apiUrl}/telegram/settings/bot/api-token/clear`, request)
      .pipe(map((response) => response.version));
  }
}
