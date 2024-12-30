import { ChangeDetectionStrategy, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { AccordionService } from '../accordion/accordion.service';
import { ReplaySubject, Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-accordion-item',
  templateUrl: './accordion-item.component.html',
  styleUrls: ['./accordion-item.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccordionItemComponent implements OnInit, OnDestroy {
  private readonly id: string = crypto.randomUUID();

  @Input('label')
  set setLabel(label: string) {
    this.label = label;
  }

  protected label: string = '';

  protected readonly opened$: Subject<boolean> = new ReplaySubject(1);

  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(private readonly accordionService: AccordionService) {}

  ngOnDestroy(): void {
    this.accordionService.unregister(this.id);

    this.opened$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnInit(): void {
    this.accordionService.register(this.id);

    this.accordionService
      .isOpened(this.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe((isOpened) => this.opened$.next(isOpened));
  }

  open(): void {
    this.accordionService.open(this.id);
  }
}
