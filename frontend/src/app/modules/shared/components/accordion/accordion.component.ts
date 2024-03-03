import { ChangeDetectionStrategy, Component } from '@angular/core';
import { AccordionService } from './accordion.service';

@Component({
  selector: 'app-accordion',
  templateUrl: './accordion.component.html',
  styleUrls: ['./accordion.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [AccordionService],
})
export class AccordionComponent {}
