import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { ContactFormResult } from '../../components';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';

@Component({
  selector: 'app-contact-page',
  templateUrl: './contact.page.html',
  styleUrls: ['./contact.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContactPage implements OnDestroy {
  protected readonly sendingFailed$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) {}

  ngOnDestroy(): void {
    this.sendingFailed$.complete();
  }

  sendMessage(result: ContactFormResult): void {
    this.sendingFailed$.next(false);

    // TODO Send to backend
    console.log(result);
    setTimeout(() => {
      const isFailure = Math.random() < 0.5;
      if (isFailure) {
        this.sendingFailed$.next(true);
        result.cancel();
      } else {
        this.router.navigate(['sent'], { relativeTo: this.route });
      }
    }, 3000);
  }
}
