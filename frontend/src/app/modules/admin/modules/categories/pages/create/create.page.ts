import { ChangeDetectionStrategy, Component } from '@angular/core';
import { CategoriesService } from '../../services';
import { CLOTHING } from '../../model';

@Component({
  selector: 'app-create-page',
  templateUrl: './create.page.html',
  styleUrls: ['./create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreatePage {
  constructor(private readonly categoriesService: CategoriesService) {
    // TODO Remove when page is implemented
    categoriesService
      .createCategory({ name: 'Neue Kategorie', group: CLOTHING })
      .subscribe();
  }
}
