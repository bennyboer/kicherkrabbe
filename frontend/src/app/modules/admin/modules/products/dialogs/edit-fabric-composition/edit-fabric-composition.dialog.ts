import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FabricComposition, Product } from '../../model';
import { Dialog, DialogService } from '../../../../../shared/modules/dialog';
import { ProductsService } from '../../services';

export interface EditFabricCompositionDialogResult {
  version: number;
  fabricComposition: FabricComposition;
}

@Component({
  selector: 'app-edit-fabric-composition-dialog',
  templateUrl: './edit-fabric-composition.dialog.html',
  styleUrls: ['./edit-fabric-composition.dialog.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class EditFabricCompositionDialog {
  constructor(
    private readonly product: Product,
    private readonly dialog: Dialog<EditFabricCompositionDialogResult>,
    private readonly dialogService: DialogService,
    private readonly productsService: ProductsService,
  ) {}
}
