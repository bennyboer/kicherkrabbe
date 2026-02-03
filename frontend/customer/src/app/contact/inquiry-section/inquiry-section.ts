import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { BehaviorSubject, finalize, map, Observable, Subject, takeUntil } from 'rxjs';
import { ContactForm, ContactFormResult } from '../contact-form/contact-form';
import { InquiriesService, InquiriesStatus, Sender } from '../inquiries.service';

@Component({
	selector: 'app-inquiry-section',
	templateUrl: './inquiry-section.html',
	styleUrl: './inquiry-section.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
	standalone: true,
	imports: [AsyncPipe, ContactForm],
})
export class InquirySection implements OnInit, OnDestroy {
	private readonly loadingInquiriesStatus$: BehaviorSubject<boolean> =
		new BehaviorSubject<boolean>(true);
	private readonly inquiriesStatus$: BehaviorSubject<InquiriesStatus> =
		new BehaviorSubject<InquiriesStatus>(InquiriesStatus.of({ enabled: false }));
	private readonly loadedInquiriesStatus$: BehaviorSubject<boolean> =
		new BehaviorSubject<boolean>(false);
	private readonly sending$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
	private readonly requestId$: BehaviorSubject<string> = new BehaviorSubject<string>(
		crypto.randomUUID()
	);

	protected readonly loading$: Observable<boolean> = this.loadingInquiriesStatus$.asObservable();
	protected readonly loaded$: Observable<boolean> = this.loadedInquiriesStatus$.asObservable();
	protected readonly inquiriesEnabled$: Observable<boolean> = this.inquiriesStatus$.pipe(
		map((status) => status.enabled)
	);

	private readonly destroy$: Subject<void> = new Subject<void>();

	constructor(
		private readonly router: Router,
		private readonly messageService: MessageService,
		private readonly inquiriesService: InquiriesService
	) {}

	ngOnInit(): void {
		this.reloadInquiriesStatus();
	}

	ngOnDestroy(): void {
		this.loadingInquiriesStatus$.complete();
		this.inquiriesStatus$.complete();
		this.loadedInquiriesStatus$.complete();
		this.requestId$.complete();
		this.sending$.complete();

		this.destroy$.next();
		this.destroy$.complete();
	}

	sendMessage(result: ContactFormResult): void {
		if (this.sending$.value) {
			return;
		}
		this.sending$.next(true);

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
			.pipe(
				takeUntil(this.destroy$),
				finalize(() => this.sending$.next(false))
			)
			.subscribe({
				next: () => this.router.navigate(['/contact/sent']),
				error: () => {
					this.messageService.add({
						severity: 'error',
						summary: 'Fehler',
						detail:
							'Beim Senden Ihrer Nachricht ist ein Fehler aufgetreten. Bitte versuchen Sie es später erneut.',
						life: 5000,
					});
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
				takeUntil(this.destroy$),
				finalize(() => {
					this.loadingInquiriesStatus$.next(false);
					this.loadedInquiriesStatus$.next(true);
				})
			)
			.subscribe({
				next: (status) => this.inquiriesStatus$.next(status),
				error: () => {
					this.messageService.add({
						severity: 'error',
						summary: 'Fehler',
						detail:
							'Das Kontaktformular konnte nicht geladen werden. Bitte versuchen Sie es später erneut.',
						life: 5000,
					});
				},
			});
	}
}
