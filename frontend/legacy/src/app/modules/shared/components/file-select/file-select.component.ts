import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  Renderer2,
} from '@angular/core';
import { BehaviorSubject, fromEvent, map, Observable, race, Subject, take, takeUntil } from 'rxjs';
import { none, Option, someOrNone } from '../../modules/option';

@Component({
  selector: 'app-file-select',
  templateUrl: './file-select.component.html',
  styleUrls: ['./file-select.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class FileSelectComponent implements OnInit, OnDestroy {
  private readonly droppable$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  private readonly destroy$: Subject<void> = new Subject<void>();

  @Input()
  label: string = 'Datei';

  @Input()
  accept: string = '';

  @Input()
  multiple: boolean = false;

  @Output()
  selected: EventEmitter<File[]> = new EventEmitter<File[]>();

  constructor(
    private readonly elementRef: ElementRef,
    private readonly renderer: Renderer2,
  ) {}

  ngOnInit(): void {
    this.droppable$.subscribe((droppable) => {
      if (droppable) {
        this.renderer.addClass(this.elementRef.nativeElement, 'droppable');
      } else {
        this.renderer.removeClass(this.elementRef.nativeElement, 'droppable');
      }
    });
  }

  ngOnDestroy(): void {
    this.droppable$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();

    this.droppable$.next(true);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();

    this.droppable$.next(false);

    const files = event.dataTransfer?.files;
    if (files) {
      this.onFilesSelected(Array.from(files));
    }
  }

  onDragLeave(_event: DragEvent): void {
    this.droppable$.next(false);
  }

  isDroppable(): Observable<boolean> {
    return this.droppable$.asObservable();
  }

  openFilePicker(fileInput: HTMLInputElement): void {
    fileInput.click();

    const filesSelected$ = fromEvent(fileInput, 'change').pipe(
      map((event) => {
        const target = event.target as HTMLInputElement;
        const files = Array.from(target.files || []);

        return someOrNone(files);
      }),
    );
    const cancelled$: Observable<Option<File[]>> = fromEvent(fileInput, 'cancel').pipe(map(() => none()));
    const fileSelectedOrCancelled$ = race(filesSelected$, cancelled$).pipe(take(1));

    fileSelectedOrCancelled$
      .pipe(takeUntil(this.destroy$))
      .subscribe((files) => files.ifSome((f) => this.onFilesSelected(f)));
  }

  onFilesSelected(files: File[]): void {
    this.selected.emit(files);
  }
}
