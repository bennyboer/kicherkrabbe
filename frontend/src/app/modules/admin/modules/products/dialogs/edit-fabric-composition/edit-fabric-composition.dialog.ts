import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import {
  FABRIC_TYPES,
  FabricComposition,
  FabricCompositionItem,
  FabricCompositionValidationError,
  FabricType,
} from '../../model';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { ProductsService } from '../../services';
import { BehaviorSubject, map, shareReplay } from 'rxjs';
import { ButtonSize, DropdownComponent, DropdownItemId, NotificationService } from '../../../../../shared';
import { none, Option, some, someOrNone } from '../../../../../shared/modules/option';
import { validateProps } from '../../../../../../util';

export class EditFabricCompositionDialogData {
  readonly product: Option<{ id: string; version: number }>;
  readonly fabricComposition: Option<FabricComposition>;

  private constructor(props: {
    product: Option<{ id: string; version: number }>;
    fabricComposition: Option<FabricComposition>;
  }) {
    validateProps(props);

    this.product = props.product;
    this.fabricComposition = props.fabricComposition;
  }

  static of(props: {
    product?: { id: string; version: number };
    fabricComposition?: FabricComposition | null;
  }): EditFabricCompositionDialogData {
    return new EditFabricCompositionDialogData({
      product: someOrNone(props.product),
      fabricComposition: someOrNone(props.fabricComposition),
    });
  }
}

export interface EditFabricCompositionDialogResult {
  version: number;
  fabricComposition: FabricComposition;
}

interface PendingCompositionItem {
  fabricType: FabricType;
  percentage: number;
}

@Component({
  selector: 'app-edit-fabric-composition-dialog',
  templateUrl: './edit-fabric-composition.dialog.html',
  styleUrls: ['./edit-fabric-composition.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class EditFabricCompositionDialog implements OnDestroy {
  protected readonly compositionItems$ = new BehaviorSubject<PendingCompositionItem[]>(
    this.data.fabricComposition
      .map((composition) =>
        composition.items.map((item) => ({
          fabricType: item.fabricType,
          percentage: item.percentage,
        })),
      )
      .orElse([]),
  );
  protected readonly fabricTypes$ = new BehaviorSubject<FabricType[]>(FABRIC_TYPES);

  protected readonly saving$ = new BehaviorSubject<boolean>(false);

  protected readonly fabricTypesDropdownItems$ = this.fabricTypes$.pipe(
    map((fabricTypes) => {
      const items = fabricTypes.map((fabricType) => ({
        id: fabricType.internal,
        label: fabricType.label,
      }));
      items.sort((a, b) => a.label.localeCompare(b.label, 'de-de', { numeric: true }));
      return items;
    }),
  );
  protected readonly error$ = this.compositionItems$.pipe(
    map((items) => {
      try {
        this.toFabricComposition(items);
        return none<string>();
      } catch (e) {
        return some(this.getCompositionValidationErrorMessage(e as Error));
      }
    }),
    shareReplay({
      bufferSize: 1,
      refCount: true,
    }),
  );
  protected readonly hasError$ = this.error$.pipe(map((error) => error.isSome()));

  protected readonly ButtonSize = ButtonSize;

  constructor(
    protected readonly data: EditFabricCompositionDialogData,
    private readonly dialog: Dialog<EditFabricCompositionDialogResult>,
    private readonly dialogService: DialogService,
    private readonly productsService: ProductsService,
    private readonly notificationService: NotificationService,
  ) {}

  ngOnDestroy(): void {
    this.compositionItems$.complete();
    this.fabricTypes$.complete();
    this.saving$.complete();
  }

  updateFabricTypeForItem(dropdown: DropdownComponent, index: number, selection: DropdownItemId[]): void {
    if (selection.length !== 1) {
      return;
    }

    const fabricType = FABRIC_TYPES.find((fabricType) => fabricType.internal === selection[0]);
    if (!fabricType) {
      return;
    }

    const pendingItem = this.compositionItems$.value[index];
    if (!pendingItem) {
      return;
    }
    pendingItem.fabricType = fabricType;
    this.compositionItems$.next(this.compositionItems$.value);

    dropdown.toggleOpened();
  }

  updatePercentageForItem(index: number, percentage: number): void {
    const pendingItem = this.compositionItems$.value[index];
    if (!pendingItem) {
      return;
    }
    pendingItem.percentage = percentage;
    this.compositionItems$.next(this.compositionItems$.value);
  }

  addCompositionItem(): void {
    this.compositionItems$.next([...this.compositionItems$.value, { fabricType: FABRIC_TYPES[0], percentage: 0 }]);
  }

  removeCompositionItem(index: number): void {
    this.compositionItems$.next(this.compositionItems$.value.filter((_, i) => i !== index));
  }

  save(): void {
    if (this.saving$.value) {
      return;
    }
    this.saving$.next(true);

    const composition = this.toFabricComposition(this.compositionItems$.value);

    this.data.product.ifSomeOrElse(
      (product) =>
        this.productsService
          .updateFabricComposition({
            id: product.id,
            version: product.version,
            fabricComposition: composition,
          })
          .subscribe({
            next: (version) => {
              this.notificationService.publish({
                message: 'Die Stoffzusammensetzung wurde erfolgreich gespeichert',
                type: 'success',
              });

              const result: EditFabricCompositionDialogResult = {
                version,
                fabricComposition: composition,
              };

              this.dialog.attachResult(result);
              this.dialogService.close(this.dialog.id);
            },
            error: (e) => {
              console.error(e);
              this.notificationService.publish({
                message: 'Die Stoffzusammensetzung konnte nicht gespeichert werden. Bitte versuchen Sie es erneut.',
                type: 'error',
              });
            },
          }),
      () => {
        this.dialog.attachResult({
          version: 0,
          fabricComposition: composition,
        });
        this.dialogService.close(this.dialog.id);
      },
    );
  }

  private toFabricComposition(items: PendingCompositionItem[]): FabricComposition {
    return FabricComposition.of({
      items: items.map((item) =>
        FabricCompositionItem.of({ fabricType: item.fabricType, percentage: item.percentage }),
      ),
    });
  }

  private getCompositionValidationErrorMessage(e: Error): string {
    const errorType = (e as Error).message as FabricCompositionValidationError;

    switch (errorType) {
      case FabricCompositionValidationError.NO_ITEMS:
        return 'Es muss immer mindestens ein Eintrag in der Stoffzusammensetzung vorhanden sein';
      case FabricCompositionValidationError.DUPLICATE_FABRIC_TYPES:
        return 'Die Stoffzusammensetzung darf keine doppelten Stoffarten enthalten';
      case FabricCompositionValidationError.INVALID_PERCENTAGE_SUM:
        return 'Die Summe der Prozentwerte muss 100 ergeben';
      default:
        return 'Ein Fehler ist aufgetreten';
    }
  }
}
