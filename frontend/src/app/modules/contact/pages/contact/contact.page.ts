import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ContactFormResult } from '../../components';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, finalize, map, Observable } from 'rxjs';
import { InquiriesService } from '../../services';
import { InquiriesStatus } from '../../models';

@Component({
  selector: 'app-contact-page',
  templateUrl: './contact.page.html',
  styleUrls: ['./contact.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContactPage implements OnInit, OnDestroy {
  protected readonly loadingInquiriesStatus$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);
  protected readonly inquiriesStatus$: BehaviorSubject<InquiriesStatus> =
    new BehaviorSubject<InquiriesStatus>(
      InquiriesStatus.of({ enabled: false }),
    );
  protected readonly loadedInquiriesStatus$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  protected readonly sendingFailed$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);

  protected readonly loading$: Observable<boolean> =
    this.loadingInquiriesStatus$.asObservable();
  protected readonly inquiriesEnabled$: Observable<boolean> =
    this.inquiriesStatus$.pipe(map((status) => status.enabled));

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly inquiriesService: InquiriesService,
  ) {}

  ngOnInit(): void {
    this.reloadInquiriesStatus();
  }

  ngOnDestroy(): void {
    this.sendingFailed$.complete();
    this.loadingInquiriesStatus$.complete();
    this.inquiriesStatus$.complete();
    this.loadedInquiriesStatus$.complete();
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

  private reloadInquiriesStatus(): void {
    this.loadingInquiriesStatus$.next(true);

    this.inquiriesService
      .getStatus()
      .pipe(
        finalize(() => {
          this.loadingInquiriesStatus$.next(false);
          this.loadedInquiriesStatus$.next(true);
        }),
      )
      .subscribe((status) => this.inquiriesStatus$.next(status));
  }
}
