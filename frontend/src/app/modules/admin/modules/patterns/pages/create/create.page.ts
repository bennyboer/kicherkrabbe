import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { BehaviorSubject, combineLatest, map, Observable } from 'rxjs';
import { environment } from '../../../../../../../environments';

@Component({
  selector: 'app-create-pattern-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreatePage implements OnDestroy {
  private readonly name$: BehaviorSubject<string> = new BehaviorSubject<string>(
    '',
  );
  private readonly nameTouched$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);
  private readonly nameValid$: Observable<boolean> = this.name$.pipe(
    map((name) => name.length > 0),
  );
  protected readonly nameError$: Observable<boolean> = combineLatest([
    this.nameTouched$,
    this.nameValid$,
  ]).pipe(map(([touched, valid]) => touched && !valid));

  protected readonly imageIds$: BehaviorSubject<string[]> = new BehaviorSubject<
    string[]
  >([]);
  protected readonly imageUploadActive$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(true);

  private readonly originalPatternName$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');
  private readonly attribution$: BehaviorSubject<string> =
    new BehaviorSubject<string>('');

  protected readonly creating$: BehaviorSubject<boolean> =
    new BehaviorSubject<boolean>(false);

  protected readonly cannotSubmit$: Observable<boolean> = this.nameValid$.pipe(
    map((valid) => !valid),
  );

  ngOnDestroy(): void {
    this.name$.complete();
    this.nameTouched$.complete();
    this.creating$.complete();
    this.originalPatternName$.complete();
    this.attribution$.complete();
    this.imageIds$.complete();
    this.imageUploadActive$.complete();
  }

  create(): void {
    this.creating$.next(true);

    const name = this.name$.value;
    const originalPatternName = this.originalPatternName$.value;
    const attribution = this.attribution$.value;
    const imageIds = this.imageIds$.value;

    console.log(
      'Create pattern',
      name,
      originalPatternName,
      attribution,
      imageIds,
    ); // TODO
  }

  updateName(value: string): void {
    this.name$.next(value.trim());

    if (!this.nameTouched$.value) {
      this.nameTouched$.next(true);
    }
  }

  updateOriginalPatternName(value: string): void {
    this.originalPatternName$.next(value.trim());
  }

  updateAttribution(value: string): void {
    this.attribution$.next(value.trim());
  }

  onImagesUploaded(imageIds: string[]): void {
    this.imageUploadActive$.next(false);
    this.imageIds$.next([...this.imageIds$.value, ...imageIds]);
  }

  activateImageUpload(): void {
    this.imageUploadActive$.next(true);
  }

  getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }
}
