import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Theme, ThemeService } from '../../services';
import { map, Observable } from 'rxjs';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class FooterComponent {
  constructor(private readonly themeService: ThemeService) {}

  isDarkMode(): Observable<boolean> {
    return this.themeService.getTheme().pipe(map((theme) => theme === Theme.DARK));
  }

  isLightMode(): Observable<boolean> {
    return this.themeService.getTheme().pipe(map((theme) => theme === Theme.LIGHT));
  }
}
