import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { ContactFormResult } from '../../components';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, finalize, map, Observable } from 'rxjs';
import { InquiriesService } from '../../services';
import { InquiriesStatus, Sender } from '../../models';

@Component({
    selector: 'app-contact-page',
    templateUrl: './contact.page.html',
    styleUrls: ['./contact.page.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class ContactPage implements OnInit, OnDestroy {
  protected readonly loadingInquiriesStatus$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
  protected readonly inquiriesStatus$: BehaviorSubject<InquiriesStatus> = new BehaviorSubject<InquiriesStatus>(
    InquiriesStatus.of({ enabled: false }),
  );
  protected readonly loadedInquiriesStatus$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly sending$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly sendingFailed$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly requestId$: BehaviorSubject<string> = new BehaviorSubject<string>(crypto.randomUUID());

  protected readonly loading$: Observable<boolean> = this.loadingInquiriesStatus$.asObservable();
  protected readonly inquiriesEnabled$: Observable<boolean> = this.inquiriesStatus$.pipe(
    map((status) => status.enabled),
  );

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
    this.requestId$.complete();
    this.sending$.complete();
  }

  sendMessage(result: ContactFormResult): void {
    if (this.sending$.value) {
      return;
    }
    this.sending$.next(true);
    this.sendingFailed$.next(false);

    const requestId = this.requestId$.value;

    this.inquiriesService
      .send({
        requestId,
        sender: Sender.of({
          name: result.name,
          mail: result.mail,
          phone: result.phone?.filter((phone) => phone.trim().length > 0).orElseNull(),
        }),
        subject: result.subject,
        message: JSON.stringify(result.message),
      })
      .pipe(finalize(() => this.sending$.next(false)))
      .subscribe({
        next: () => this.router.navigate(['sent'], { relativeTo: this.route }),
        error: () => {
          this.sendingFailed$.next(true);
          result.cancel();
          this.requestId$.next(crypto.randomUUID());
        },
      });
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
