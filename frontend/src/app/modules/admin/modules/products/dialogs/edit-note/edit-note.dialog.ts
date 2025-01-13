import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { NotificationService } from '../../../../../shared';
import { ProductsService } from '../../services';
import { someOrNone } from '../../../../../shared/modules/option';
import { ContentChange } from 'ngx-quill';
import Quill, { Delta } from 'quill/core';
import { BehaviorSubject, combineLatest, finalize, first, ReplaySubject, Subject, takeUntil } from 'rxjs';
import { Notes, Product } from '../../model';

export interface EditNoteDialogResult {
  version: number;
  notes: Notes;
}

export enum NoteType {
  CONTAINS = 'CONTAINS',
  CARE = 'CARE',
  SAFETY = 'SAFETY',
}

@Component({
  selector: 'app-edit-note-dialog',
  templateUrl: './edit-note.dialog.html',
  styleUrls: ['./edit-note.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class EditNoteDialog implements OnDestroy, OnInit {
  protected readonly note$ = new BehaviorSubject<Delta>(new Delta());
  protected readonly saving$ = new BehaviorSubject<boolean>(false);
  private readonly editor$: Subject<Quill> = new ReplaySubject(1);
  private readonly destroy$ = new Subject<void>();

  protected readonly cannotSave$ = this.saving$.asObservable();
  protected readonly loading$ = this.saving$.asObservable();

  protected readonly quillModules = {
    toolbar: [
      [{ header: [1, 2, false] }],
      ['bold', 'italic', 'underline', 'strike', 'link'],
      [{ align: [] }],
      [{ color: [] }, { background: [] }],
      ['blockquote', { list: 'ordered' }, { list: 'bullet' }],
      ['clean'],
    ],
  };

  constructor(
    private readonly noteType: NoteType,
    private readonly product: Product,
    private readonly dialog: Dialog<EditNoteDialogResult>,
    private readonly dialogService: DialogService,
    private readonly productsService: ProductsService,
    private readonly notificationService: NotificationService,
  ) {
    let note = '';
    switch (noteType) {
      case NoteType.CONTAINS:
        note = product.notes.contains;
        break;
      case NoteType.CARE:
        note = product.notes.care;
        break;
      case NoteType.SAFETY:
        note = product.notes.safety;
        break;
      default:
        throw new Error(`Unknown note type: ${noteType}`);
    }

    this.note$.next(note.length > 0 ? new Delta(JSON.parse(note)) : new Delta());
  }

  ngOnInit(): void {
    combineLatest([this.editor$, this.note$])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([editor, note]) => {
        const currentContents = editor.getContents();
        const expectedContents = note;

        if (expectedContents.ops.length === 0) {
          return;
        }

        if (JSON.stringify(currentContents) !== JSON.stringify(expectedContents)) {
          editor.setContents(note);
          editor.focus();
        }
      });
  }

  ngOnDestroy(): void {
    this.note$.complete();
    this.saving$.complete();
    this.editor$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  onEditorCreated(quill: Quill): void {
    this.editor$.next(quill);
  }

  updateNote(event: ContentChange): void {
    const html = someOrNone(event.html)
      .map((h) => h.trim())
      .orElse('');
    const isEmpty = html.length === 0;
    if (isEmpty) {
      this.note$.next(new Delta());
    } else {
      this.note$.next(event.content);
    }
  }

  cancel(): void {
    this.dialogService.close(this.dialog.id);
  }

  save(): void {
    if (this.saving$.value) {
      return;
    }
    this.saving$.next(true);

    const note = this.note$.value.ops.length > 0 ? JSON.stringify(this.note$.value) : '';

    let updatedNotes = this.product.notes;
    switch (this.noteType) {
      case NoteType.CONTAINS:
        updatedNotes = updatedNotes.updateContains(note);
        break;
      case NoteType.CARE:
        updatedNotes = updatedNotes.updateCare(note);
        break;
      case NoteType.SAFETY:
        updatedNotes = updatedNotes.updateSafety(note);
        break;
      default:
        throw new Error(`Unknown note type: ${this.noteType}`);
    }

    this.productsService
      .updateNotes({
        id: this.product.id,
        version: this.product.version,
        notes: updatedNotes,
      })
      .pipe(
        first(),
        finalize(() => this.saving$.next(false)),
      )
      .subscribe({
        next: (version) => {
          this.notificationService.publish({
            message: `${this.getNoteType()} wurde gespeichert.`,
            type: 'success',
          });

          this.dialog.attachResult({
            version,
            notes: updatedNotes,
          });
          this.dialogService.close(this.dialog.id);
        },
        error: (e) => {
          console.error('Failed to save note', e);
          this.notificationService.publish({
            message: `${this.getNoteType()} konnte nicht gespeichert werden. Bitte versuche es erneut.`,
            type: 'error',
          });
        },
      });
  }

  private getNoteType(): string {
    switch (this.noteType) {
      case NoteType.CONTAINS:
        return 'Inhaltsangabe';
      case NoteType.CARE:
        return 'Pflegehinweise';
      case NoteType.SAFETY:
        return 'Sicherheitshinweise';
      default:
        throw new Error(`Unknown note type: ${this.noteType}`);
    }
  }
}
