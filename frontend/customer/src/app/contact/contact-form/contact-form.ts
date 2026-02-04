import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  OnDestroy,
  Output,
} from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { BehaviorSubject, combineLatest, map, Observable } from 'rxjs';
import { Option, someOrNone, validateProps } from '@kicherkrabbe/shared';
import { RouterLink } from '@angular/router';
import { InputText } from 'primeng/inputtext';
import { Button } from 'primeng/button';
import { ProgressSpinner } from 'primeng/progressspinner';
import { QuillEditorWrapper } from '../quill-editor-wrapper/quill-editor-wrapper';

export interface MessageContent {
  ops: unknown[];
  length(): number;
}

export class ContactFormResult {
  readonly name: string;
  readonly mail: string;
  readonly phone: Option<string>;
  readonly subject: string;
  readonly message: MessageContent;
  readonly cancel: () => void;

  private constructor(props: {
    name: string;
    mail: string;
    phone: Option<string>;
    subject: string;
    message: MessageContent;
    cancel: () => void;
  }) {
    validateProps(props);

    this.name = props.name;
    this.mail = props.mail;
    this.phone = props.phone;
    this.subject = props.subject;
    this.message = props.message;
    this.cancel = props.cancel;
  }

  static of(props: {
    name: string;
    mail: string;
    phone?: string;
    subject: string;
    message: MessageContent;
    cancel: () => void;
  }): ContactFormResult {
    return new ContactFormResult({
      name: props.name,
      mail: props.mail,
      phone: someOrNone(props.phone),
      subject: props.subject,
      message: props.message,
      cancel: props.cancel,
    });
  }
}

const emptyMessage: MessageContent = {
  ops: [],
  length: () => 0,
};

@Component({
  selector: 'app-contact-form',
  templateUrl: './contact-form.html',
  styleUrl: './contact-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    AsyncPipe,
    RouterLink,
    InputText,
    Button,
    ProgressSpinner,
    QuillEditorWrapper,
  ],
})
export class ContactForm implements OnDestroy {
  @Output()
  submitted: EventEmitter<ContactFormResult> = new EventEmitter<ContactFormResult>();

  protected readonly submitting$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  private readonly name$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  protected readonly nameTouched$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly nameLength$: Observable<number> = this.name$.pipe(map((name) => name.length));
  protected readonly nameMissing$: Observable<boolean> = this.name$.pipe(
    map((name) => name.length === 0)
  );
  protected readonly nameTooLong$: Observable<boolean> = this.name$.pipe(
    map((name) => name.length > 200)
  );
  protected readonly nameDisabled$: Observable<boolean> = this.submitting$;
  protected readonly nameValid$: Observable<boolean> = combineLatest([
    this.nameMissing$,
    this.nameTooLong$,
  ]).pipe(map(([missing, tooLong]) => !missing && !tooLong));

  private readonly mail$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  protected readonly mailTouched$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly mailLength$: Observable<number> = this.mail$.pipe(map((mail) => mail.length));
  protected readonly mailMissing$: Observable<boolean> = this.mail$.pipe(
    map((mail) => mail.length === 0)
  );
  protected readonly mailTooLong$: Observable<boolean> = this.mail$.pipe(
    map((mail) => mail.length > 70)
  );
  protected readonly mailFormatInvalid$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(
    false
  );
  protected readonly mailDisabled$: Observable<boolean> = this.submitting$;
  protected readonly mailValid$: Observable<boolean> = combineLatest([
    this.mailMissing$,
    this.mailTooLong$,
    this.mailFormatInvalid$,
  ]).pipe(map(([missing, tooLong, formatInvalid]) => !missing && !tooLong && !formatInvalid));

  private readonly phone$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  protected readonly phoneTouched$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  protected readonly phoneLength$: Observable<number> = this.phone$.pipe(
    map((phone) => phone.length)
  );
  protected readonly phoneTooLong$: Observable<boolean> = this.phone$.pipe(
    map((phone) => phone.length > 30)
  );
  protected readonly phoneDisabled$: Observable<boolean> = this.submitting$;
  protected readonly phoneValid$: Observable<boolean> = this.phoneTooLong$.pipe(
    map((tooLong) => !tooLong)
  );

  private readonly subject$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  protected readonly subjectTouched$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(
    false
  );
  protected readonly subjectLength$: Observable<number> = this.subject$.pipe(
    map((subject) => subject.length)
  );
  protected readonly subjectMissing$: Observable<boolean> = this.subject$.pipe(
    map((subject) => subject.length === 0)
  );
  protected readonly subjectTooLong$: Observable<boolean> = this.subject$.pipe(
    map((subject) => subject.length > 200)
  );
  protected readonly subjectDisabled$: Observable<boolean> = this.submitting$;
  protected readonly subjectValid$: Observable<boolean> = combineLatest([
    this.subjectMissing$,
    this.subjectTooLong$,
  ]).pipe(map(([missing, tooLong]) => !missing && !tooLong));

  private readonly message$: BehaviorSubject<MessageContent> =
    new BehaviorSubject<MessageContent>(emptyMessage);
  protected readonly messageHtml$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  protected readonly messageTouched$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(
    false
  );
  protected readonly messageMissing$: Observable<boolean> = this.message$.pipe(
    map((message) => message.length() === 0)
  );
  protected readonly messageTooLong$: Observable<boolean> = this.message$.pipe(
    map((message) => message.length() > 10000)
  );
  protected readonly messageHtmlTooLong$: Observable<boolean> = this.messageHtml$.pipe(
    map((messageHtml) => messageHtml.length > 20000)
  );
  protected readonly messageValid$: Observable<boolean> = combineLatest([
    this.messageMissing$,
    this.messageTooLong$,
    this.messageHtmlTooLong$,
  ]).pipe(map(([missing, tooLong, htmlTooLong]) => !missing && !tooLong && !htmlTooLong));

  protected readonly canSubmit$: Observable<boolean> = combineLatest([
    this.submitting$,
    this.nameValid$,
    this.mailValid$,
    this.phoneValid$,
    this.subjectValid$,
    this.messageValid$,
  ]).pipe(
    map(
      ([submitting, nameValid, mailValid, phoneValid, subjectValid, messageValid]) =>
        !submitting && nameValid && mailValid && phoneValid && subjectValid && messageValid
    )
  );
  protected readonly cannotSubmit$: Observable<boolean> = this.canSubmit$.pipe(
    map((canSubmit) => !canSubmit)
  );

  protected readonly quillModules = {
    toolbar: [[{ header: [1, 2, false] }], ['bold', 'italic', 'underline'], ['clean']],
  };

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

  updateMessage(event: { html?: string; content: unknown }): void {
    const html = someOrNone(event.html)
      .map((h) => h.trim())
      .orElse('');
    this.messageHtml$.next(html);

    const isEmpty = html.length === 0;
    if (isEmpty) {
      this.message$.next(emptyMessage);
    } else {
      this.message$.next(event.content as MessageContent);
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

    this.submitting$.next(true);
    const result = ContactFormResult.of({
      name,
      mail,
      phone,
      subject,
      message,
      cancel: () => {
        this.submitting$.next(false);
      },
    });

    this.submitted.emit(result);
  }
}
