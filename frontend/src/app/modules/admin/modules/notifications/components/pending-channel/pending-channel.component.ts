import { ChangeDetectionStrategy, Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core';
import { Channel, EMAIL, Telegram, TELEGRAM } from '../../model';
import { BehaviorSubject, combineLatest, map } from 'rxjs';

@Component({
  selector: 'app-pending-channel',
  templateUrl: './pending-channel.component.html',
  styleUrls: ['./pending-channel.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PendingChannelComponent implements OnDestroy {
  @Input()
  channel!: Channel;

  @Input()
  label: string = '';

  @Output()
  done = new EventEmitter<Channel>();

  @Output()
  cancelled = new EventEmitter<void>();

  protected readonly mail$ = new BehaviorSubject<string>('');
  protected readonly mailTouched$ = new BehaviorSubject<boolean>(false);
  protected readonly mailValid$ = new BehaviorSubject<boolean>(false);
  protected readonly mailInvalid$ = this.mailValid$.pipe(map((valid) => !valid));
  protected readonly mailInvalidAndTouched$ = combineLatest([this.mailInvalid$, this.mailTouched$]).pipe(
    map(([invalid, touched]) => invalid && touched),
  );

  protected readonly telegramChatId$ = new BehaviorSubject<string>('');
  protected readonly telegramChatIdValid$ = new BehaviorSubject<boolean>(false);
  protected readonly telegramChatIdInvalid$ = this.telegramChatIdValid$.pipe(map((valid) => !valid));

  protected readonly EMAIL = EMAIL;
  protected readonly TELEGRAM = TELEGRAM;

  ngOnDestroy(): void {
    this.mail$.complete();
    this.mailTouched$.complete();
    this.mailValid$.complete();

    this.telegramChatId$.complete();
    this.telegramChatIdValid$.complete();
  }

  updateMail(value: string, valid: boolean): void {
    this.mail$.next(value);
    this.mailValid$.next(valid);

    if (!this.mailTouched$.value) {
      this.mailTouched$.next(true);
    }
  }

  updateTelegramChatId(value: string): void {
    this.telegramChatId$.next(value);

    if (value.trim().length > 0) {
      this.telegramChatIdValid$.next(true);
    }
  }

  cancel(): void {
    this.cancelled.emit();
  }

  finishEmail(): void {
    if (!this.mailValid$.value) {
      return;
    }

    const mail = this.mail$.value;
    const result = Channel.mail(mail);

    this.done.emit(result);
  }

  finishTelegram(): void {
    if (!this.telegramChatIdValid$.value) {
      return;
    }

    const telegramChatId = this.telegramChatId$.value;
    const telegram = Telegram.of({
      chatId: telegramChatId,
    });
    const result = Channel.telegram(telegram);

    this.done.emit(result);
  }
}
