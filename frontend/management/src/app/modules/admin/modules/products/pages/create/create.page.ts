import { ChangeDetectionStrategy, Component, Injector, OnDestroy } from '@angular/core';
import { ButtonSize, NotificationService } from '../../../../../shared';
import { BehaviorSubject, combineLatest, delay, distinctUntilChanged, finalize, first, map } from 'rxjs';
import { Theme, ThemeService } from '../../../../../../services';
import { FabricComposition, Link, Notes } from '../../model';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import {
  AddLinkDialog,
  AddLinkDialogData,
  AddLinkDialogResult,
  EditFabricCompositionDialog,
  EditFabricCompositionDialogData,
  EditFabricCompositionDialogResult,
  EditImagesDialog,
  EditImagesDialogData,
  EditImagesDialogResult,
  EditNoteDialog,
  EditNoteDialogData,
  EditNoteDialogResult,
  EditProducedAtDateDialog,
  EditProducedAtDateDialogData,
  EditProducedAtDateDialogResult,
  NoteType,
} from '../../dialogs';
import { ProductsService } from '../../services';
import { none, Option, some } from '@kicherkrabbe/shared';
import { AssetsService } from '../../../assets/services/assets.service';
import { ImageSliderImage } from '../../../../../shared/modules/image-slider';
import { environment } from '../../../../../../../environments';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-create-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class CreatePage implements OnDestroy {
  protected readonly images$ = new BehaviorSubject<string[]>([]);
  protected readonly links$ = new BehaviorSubject<Link[]>([]);
  protected readonly fabricComposition$ = new BehaviorSubject<Option<FabricComposition>>(none());
  protected readonly notes$ = new BehaviorSubject<Notes>(Notes.empty());
  protected readonly producedAt$ = new BehaviorSubject<Date>(new Date());

  protected readonly creating$ = new BehaviorSubject<boolean>(false);

  protected readonly theme$ = this.themeService
    .getTheme()
    .pipe(map((theme) => (theme === Theme.DARK ? 'dark' : 'light')));
  protected readonly fabricCompositionIsMissing$ = this.fabricComposition$.pipe(
    map((composition) => composition.isNone()),
  );
  protected readonly invalid$ = this.fabricCompositionIsMissing$;
  protected readonly cannotCreate$ = combineLatest([this.creating$, this.invalid$]).pipe(
    map(([creating, invalid]) => creating || invalid),
  );
  protected readonly imageSliderImages$ = this.images$.pipe(
    map((images) => this.toImageSliderImages(images)),
    distinctUntilChanged((a, b) => {
      if (a.length !== b.length) {
        return false;
      }

      return a.every((image, index) => image.equals(b[index]));
    }),
  );

  protected readonly ButtonSize = ButtonSize;

  constructor(
    private readonly router: Router,
    private readonly route: ActivatedRoute,
    private readonly themeService: ThemeService,
    private readonly dialogService: DialogService,
    private readonly productsService: ProductsService,
    private readonly assetsService: AssetsService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnDestroy(): void {
    this.images$.complete();
    this.links$.complete();
    this.fabricComposition$.complete();
    this.notes$.complete();
    this.producedAt$.complete();
    this.creating$.complete();
  }

  create(): void {
    if (this.creating$.value) {
      return;
    }
    this.creating$.next(true);

    this.productsService
      .createProduct({
        images: this.images$.value,
        links: this.links$.value,
        fabricComposition: this.fabricComposition$.value.orElseThrow(),
        notes: this.notes$.value,
        producedAt: this.producedAt$.value,
      })
      .pipe(
        first(),
        delay(500),
        finalize(() => this.creating$.next(false)),
      )
      .subscribe({
        next: () => {
          this.notificationService.publish({
            message: 'Produkt wurde erfolgreich erstellt.',
            type: 'success',
          });

          this.router.navigate(['..'], { relativeTo: this.route });
        },
        error: (e) => {
          console.error('Failed to create product', e);
          this.notificationService.publish({
            message: 'Produkt konnte nicht erstellt werden. Bitte versuchen Sie es erneut.',
            type: 'error',
          });
        },
      });
  }

  addLink(links: Link[]): void {
    const dialog = Dialog.create<AddLinkDialogResult>({
      title: 'Link hinzufügen',
      componentType: AddLinkDialog,
      injector: Injector.create({
        providers: [
          {
            provide: AddLinkDialogData,
            useValue: AddLinkDialogData.of({
              links,
            }),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog
        .getResult()
        .map((result) => [...links, result.link])
        .ifSome((updatedLinks) => this.links$.next(updatedLinks));
    });
  }

  removeLink(event: Event, links: Link[], link: Link): void {
    event.stopPropagation();
    event.preventDefault();

    const updatedLinks = links.filter((l) => l.id !== link.id);
    this.links$.next(updatedLinks);
  }

  editImages(images: string[]): void {
    const dialog = Dialog.create<EditImagesDialogResult>({
      title: 'Bilder bearbeiten',
      componentType: EditImagesDialog,
      injector: Injector.create({
        providers: [
          {
            provide: EditImagesDialogData,
            useValue: EditImagesDialogData.of({
              images,
            }),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
          {
            provide: AssetsService,
            useValue: this.assetsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog.getResult().ifSome((result) => this.images$.next(result.images));
    });
  }

  editFabricComposition(composition: Option<FabricComposition>): void {
    const dialog = Dialog.create<EditFabricCompositionDialogResult>({
      title: 'Stoffzusammensetzung bearbeiten',
      componentType: EditFabricCompositionDialog,
      injector: Injector.create({
        providers: [
          {
            provide: EditFabricCompositionDialogData,
            useValue: EditFabricCompositionDialogData.of({
              fabricComposition: composition.orElseNull(),
            }),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog.getResult().ifSome((result) => this.fabricComposition$.next(some(result.fabricComposition)));
    });
  }

  editContainsNote(notes: Notes): void {
    const dialog = Dialog.create<EditNoteDialogResult>({
      title: 'Inhaltsangaben bearbeiten',
      componentType: EditNoteDialog,
      injector: Injector.create({
        providers: [
          {
            provide: EditNoteDialogData,
            useValue: EditNoteDialogData.of({
              notes,
              noteType: NoteType.CONTAINS,
            }),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog.getResult().ifSome((result) => this.notes$.next(result.notes));
    });
  }

  editCareNote(notes: Notes): void {
    const dialog = Dialog.create<EditNoteDialogResult>({
      title: 'Pflegehinweise bearbeiten',
      componentType: EditNoteDialog,
      injector: Injector.create({
        providers: [
          {
            provide: EditNoteDialogData,
            useValue: EditNoteDialogData.of({
              notes,
              noteType: NoteType.CARE,
            }),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog.getResult().ifSome((result) => this.notes$.next(result.notes));
    });
  }

  editSafetyNote(notes: Notes): void {
    const dialog = Dialog.create<EditNoteDialogResult>({
      title: 'Sicherheitshinweise bearbeiten',
      componentType: EditNoteDialog,
      injector: Injector.create({
        providers: [
          {
            provide: EditNoteDialogData,
            useValue: EditNoteDialogData.of({
              notes,
              noteType: NoteType.SAFETY,
            }),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog.getResult().ifSome((result) => this.notes$.next(result.notes));
    });
  }

  editProducedAtDate(producedAt: Date): void {
    const dialog = Dialog.create<EditProducedAtDateDialogResult>({
      title: 'Produktionsdatum bearbeiten',
      componentType: EditProducedAtDateDialog,
      injector: Injector.create({
        providers: [
          {
            provide: EditProducedAtDateDialogData,
            useValue: EditProducedAtDateDialogData.of({}),
          },
          {
            provide: ProductsService,
            useValue: this.productsService,
          },
        ],
      }),
    });

    this.dialogService.open(dialog);
    this.dialogService.waitUntilClosed(dialog.id).subscribe(() => {
      dialog.getResult().ifSome((result) => this.producedAt$.next(result.date));
    });
  }

  private toImageSliderImages(images: string[]): ImageSliderImage[] {
    return images.map((image) => ImageSliderImage.of({ url: this.getImageUrl(image) }));
  }

  private getImageUrl(imageId: string): string {
    return `${environment.apiUrl}/assets/${imageId}/content`;
  }
}
