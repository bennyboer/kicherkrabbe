import { AddLinkDialog, AddLinkDialogResult } from './add-link/add-link.dialog';
import {
  EditFabricCompositionDialog,
  EditFabricCompositionDialogResult,
} from './edit-fabric-composition/edit-fabric-composition.dialog';
import { EditImagesDialog, EditImagesDialogResult } from './edit-images/edit-images.dialog';
import { EditNoteDialog, EditNoteDialogResult, NoteType } from './edit-note/edit-note.dialog';
import {
  EditProducedAtDateDialog,
  EditProducedAtDateDialogResult,
} from './edit-produced-at-date/edit-produced-at-date.dialog';

export {
  AddLinkDialog,
  AddLinkDialogResult,
  EditFabricCompositionDialog,
  EditFabricCompositionDialogResult,
  EditNoteDialog,
  EditNoteDialogResult,
  NoteType,
  EditProducedAtDateDialog,
  EditProducedAtDateDialogResult,
  EditImagesDialog,
  EditImagesDialogResult,
};

export const DIALOGS = [
  AddLinkDialog,
  EditFabricCompositionDialog,
  EditNoteDialog,
  EditProducedAtDateDialog,
  EditImagesDialog,
];
