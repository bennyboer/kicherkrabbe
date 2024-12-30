import { AfterViewInit, ChangeDetectionStrategy, Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable, Subject, takeUntil } from 'rxjs';
import { TopicsService } from '../../services';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationService } from '../../../../../shared';

@Component({
  selector: 'app-create-topic',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreateTopicPage implements AfterViewInit, OnDestroy {
  @ViewChild('name')
  nameInput!: ElementRef;

  private readonly name$: BehaviorSubject<string> = new BehaviorSubject<string>('');
  private readonly creatingTopic$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly failed$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly topicsService: TopicsService,
    private readonly notificationService: NotificationService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
  ) {}

  ngAfterViewInit(): void {
    this.nameInput.nativeElement.focus();
  }

  ngOnDestroy(): void {
    this.name$.complete();
    this.creatingTopic$.complete();
    this.failed$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateName(value: string): void {
    this.name$.next(value.trim());
  }

  createTopic(): boolean {
    const name = this.name$.value;

    this.creatingTopic$.next(true);
    this.failed$.next(false);
    this.topicsService
      .createTopic(name)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.creatingTopic$.next(false);
          this.notificationService.publish({
            message: `Das Thema „${name}“ wurde erfolgreich erstellt.`,
            type: 'success',
          });
          this.router.navigate(['..'], { relativeTo: this.route });
        },
        error: () => {
          this.failed$.next(true);
          this.creatingTopic$.next(false);
        },
      });

    return false;
  }

  isCreatingTopic(): Observable<boolean> {
    return this.creatingTopic$.asObservable();
  }

  isFailed(): Observable<boolean> {
    return this.failed$.asObservable();
  }

  isFormValid(): Observable<boolean> {
    return this.name$.pipe(map((name) => name.length > 0));
  }

  canCreateTopic(): Observable<boolean> {
    return combineLatest([this.isFormValid(), this.isCreatingTopic()]).pipe(
      map(([valid, creating]) => valid && !creating),
    );
  }

  cannotCreateTopic(): Observable<boolean> {
    return this.canCreateTopic().pipe(map((canCreate) => !canCreate));
  }
}
