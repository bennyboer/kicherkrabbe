import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { ContentChange, QuillEditorComponent } from 'ngx-quill';
import { someOrNone } from '../../../../util';
import { Delta } from 'quill/core';
import {
  BehaviorSubject,
  combineLatest,
  map,
  Observable,
  Subject,
  takeUntil,
} from 'rxjs';

@Component({
  selector: 'app-contact-form',
  templateUrl: './contact-form.component.html',
  styleUrls: ['./contact-form.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ContactFormComponent implements OnInit, OnDestroy {
  @ViewChild(QuillEditorComponent)
  quillEditor!: QuillEditorComponent;

  private readonly submitting$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);

  private readonly name$: BehaviorSubject<string> = new BehaviorSubject<string>(
    '',
  );
  protected readonly nameTouched$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  protected readonly nameLength$: Observable<number> = this.name$.pipe(
    map((name) => name.length),
  );
  protected readonly nameMissing$: Observable<boolean> = this.name$.pipe(
    map((name) => name.length === 0),
  );
  protected readonly nameTooLong$: Observable<boolean> = this.name$.pipe(
    map((name) => name.length > 200),
  );
  protected readonly nameDisabled$: Observable<boolean> = this.submitting$;
  protected readonly nameValid$: Observable<boolean> = combineLatest([
    this.nameMissing$,
    this.nameTooLong$,
  ]).pipe(map(([missing, tooLong]) => !missing && !tooLong));

  private readonly mail$: BehaviorSubject<string> = new BehaviorSubject<string>(
    '',
  );
  protected readonly mailTouched$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  protected readonly mailLength$: Observable<number> = this.mail$.pipe(
    map((mail) => mail.length),
  );
  protected readonly mailMissing$: Observable<boolean> = this.mail$.pipe(
    map((mail) => mail.length === 0),
  );
  protected readonly mailTooLong$: Observable<boolean> = this.mail$.pipe(
    map((mail) => mail.length > 200),
  );
  protected readonly mailFormatInvalid$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  protected readonly mailDisabled$: Observable<boolean> = this.submitting$;
  protected readonly mailValid$: Observable<boolean> = combineLatest([
    this.mailMissing$,
    this.mailTooLong$,
    this.mailFormatInvalid$,
  ]).pipe(
    map(
      ([missing, tooLong, formatInvalid]) =>
        !missing && !tooLong && !formatInvalid,
    ),
  );

  private readonly phone$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');
  protected readonly phoneTouched$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  protected readonly phoneLength$: Observable<number> = this.phone$.pipe(
    map((phone) => phone.length),
  );
  protected readonly phoneTooLong$: Observable<boolean> = this.phone$.pipe(
    map((phone) => phone.length > 100),
  );
  protected readonly phoneDisabled$: Observable<boolean> = this.submitting$;
  protected readonly phoneValid$: Observable<boolean> = this.phoneTooLong$.pipe(
    map((tooLong) => !tooLong),
  );

  private readonly subject$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');
  protected readonly subjectTouched$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  protected readonly subjectLength$: Observable<number> = this.subject$.pipe(
    map((subject) => subject.length),
  );
  protected readonly subjectMissing$: Observable<boolean> = this.subject$.pipe(
    map((subject) => subject.length === 0),
  );
  protected readonly subjectTooLong$: Observable<boolean> = this.subject$.pipe(
    map((subject) => subject.length > 200),
  );
  protected readonly subjectDisabled$: Observable<boolean> = this.submitting$;
  protected readonly subjectValid$: Observable<boolean> = combineLatest([
    this.subjectMissing$,
    this.subjectTooLong$,
  ]).pipe(map(([missing, tooLong]) => !missing && !tooLong));

  private readonly message$: BehaviorSubject<Delta> =
    new BehaviorSubject<Delta>(new Delta());
  protected readonly messageHtml$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');
  protected readonly messageTouched$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  protected readonly messageMissing$: Observable<boolean> = this.message$.pipe(
    map((message) => message.length() === 0),
  );
  protected readonly messageTooLong$: Observable<boolean> = this.message$.pipe(
    map((message) => message.length() > 10000),
  );
  protected readonly messageHtmlTooLong$: Observable<boolean> =
    this.messageHtml$.pipe(map((messageHtml) => messageHtml.length > 20000));
  protected readonly messageValid$: Observable<boolean> = combineLatest([
    this.messageMissing$,
    this.messageTooLong$,
    this.messageHtmlTooLong$,
  ]).pipe(
    map(
      ([missing, tooLong, htmlTooLong]) => !missing && !tooLong && !htmlTooLong,
    ),
  );

  protected readonly canSubmit$: Observable<boolean> = combineLatest([
    this.nameValid$,
    this.mailValid$,
    this.phoneValid$,
    this.subjectValid$,
    this.messageValid$,
  ]).pipe(
    map(
      ([nameValid, mailValid, phoneValid, subjectValid, messageValid]) =>
        nameValid && mailValid && phoneValid && subjectValid && messageValid,
    ),
  );
  protected readonly cannotSubmit$: Observable<boolean> = this.canSubmit$.pipe(
    map((canSubmit) => !canSubmit),
  );

  private readonly destroy$: Subject<void> = new Subject<void>();

  protected readonly quillModules = {
    toolbar: [
      [{ header: [1, 2, false] }],
      ['bold', 'italic', 'underline'],
      ['clean'],
    ],
  };

  ngOnInit(): void {
    this.submitting$.pipe(takeUntil(this.destroy$)).subscribe((submitting) => {
      if (this.quillEditor) {
        this.quillEditor.setDisabledState(submitting);
      }
    });
  }

  ngOnDestroy(): void {
    this.name$.complete();
    this.nameTouched$.complete();

    this.mail$.complete();
    this.mailTouched$.complete();
    this.mailFormatInvalid$.complete();

    this.phone$.complete();
    this.phoneTouched$.complete();

    this.subject$.complete();
    this.subjectTouched$.complete();

    this.message$.complete();
    this.messageHtml$.complete();
    this.messageTouched$.complete();

    this.submitting$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateName(value: string): void {
    this.name$.next(value.trim());

    if (!this.nameTouched$.value) {
      this.nameTouched$.next(true);
    }
  }

  updateMail(value: string, formatValid: boolean): void {
    this.mail$.next(value.trim());

    if (!this.mailTouched$.value) {
      this.mailTouched$.next(true);
    }

    this.mailFormatInvalid$.next(!formatValid);
  }

  updatePhone(value: string): void {
    this.phone$.next(value.trim());

    if (!this.phoneTouched$.value) {
      this.phoneTouched$.next(true);
    }
  }

  updateSubject(value: string): void {
    this.subject$.next(value.trim());

    if (!this.subjectTouched$.value) {
      this.subjectTouched$.next(true);
    }
  }

  updateMessage(event: ContentChange): void {
    const html = someOrNone(event.html)
      .map((h) => h.trim())
      .orElse('');
    this.messageHtml$.next(html);

    const isEmpty = html.length === 0;
    if (isEmpty) {
      this.message$.next(new Delta());
    } else {
      this.message$.next(event.content);
    }

    if (!this.messageTouched$.value) {
      this.messageTouched$.next(true);
    }
  }

  submit(): void {
    const name = this.name$.value;
    const mail = this.mail$.value;
    const phone = this.phone$.value;
    const subject = this.subject$.value;
    const message = this.message$.value;

    // TODO send data to backend
    console.log(name, mail, phone, subject, message);
    this.submitting$.next(true);
  }
}
