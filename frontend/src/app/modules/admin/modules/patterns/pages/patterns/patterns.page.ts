import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Component({
  selector: 'app-patterns-page',
  templateUrl: './patterns.page.html',
  styleUrls: ['./patterns.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatternsPage implements OnDestroy {
  protected readonly search$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');
  protected readonly loading$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);

  ngOnDestroy(): void {
    this.search$.complete();
    this.loading$.complete();
  }

  updateSearch(value: string): void {
    this.search$.next(value.trim());
  }
}
