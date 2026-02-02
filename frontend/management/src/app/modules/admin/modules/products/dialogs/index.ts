import { AddLinkDialog, AddLinkDialogData, AddLinkDialogResult } from './add-link/add-link.dialog';
import {
  EditFabricCompositionDialog,
  EditFabricCompositionDialogData,
  EditFabricCompositionDialogResult,
} from './edit-fabric-composition/edit-fabric-composition.dialog';
import { EditImagesDialog, EditImagesDialogData, EditImagesDialogResult } from './edit-images/edit-images.dialog';
import { EditNoteDialog, EditNoteDialogData, EditNoteDialogResult, NoteType } from './edit-note/edit-note.dialog';
import {
  EditProducedAtDateDialog,
  EditProducedAtDateDialogData,
  EditProducedAtDateDialogResult,
} from './edit-produced-at-date/edit-produced-at-date.dialog';

export {
  AddLinkDialog,
  AddLinkDialogData,
  AddLinkDialogResult,
  EditFabricCompositionDialog,
  EditFabricCompositionDialogData,
  EditFabricCompositionDialogResult,
  EditNoteDialog,
  EditNoteDialogData,
  EditNoteDialogResult,
  NoteType,
  EditProducedAtDateDialog,
  EditProducedAtDateDialogData,
  EditProducedAtDateDialogResult,
  EditImagesDialog,
  EditImagesDialogData,
  EditImagesDialogResult,
};

export const DIALOGS = [
  AddLinkDialog,
  EditFabricCompositionDialog,
  EditNoteDialog,
  EditProducedAtDateDialog,
  EditImagesDialog,
];
