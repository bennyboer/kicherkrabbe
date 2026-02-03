import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { ContentChange, QuillModule } from 'ngx-quill';

@Component({
  selector: 'app-quill-editor-wrapper',
  template: `
    <quill-editor
      theme="bubble"
      placeholder=""
      style="display: block; height: 400px"
      [modules]="modules"
      (onContentChanged)="onContentChanged($event)">
    </quill-editor>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [QuillModule],
})
export class QuillEditorWrapper {
  @Input() modules: object = {};
  @Output() contentChanged = new EventEmitter<{ html?: string; content: unknown }>();

  onContentChanged(event: ContentChange): void {
    this.contentChanged.emit({
      html: event.html ?? undefined,
      content: event.content,
    });
  }
}
