import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import {
  BehaviorSubject,
  filter,
  map,
  Observable,
  startWith,
  Subject,
  takeUntil,
} from 'rxjs';
import {
  DropdownComponent,
  DropdownItem,
  DropdownItemId,
} from '../../../shared';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { Option } from '../../../../util';
import { AdminAuthService } from '../../services';

class ManagementItem {
  readonly label: string;
  readonly route: string;

  private constructor(props: { label: string; route: string }) {
    this.label = props.label;
    this.route = props.route;
  }

  static of(props: { label: string; route: string }): ManagementItem {
    return new ManagementItem({
      label: props.label,
      route: props.route,
    });
  }
}

const DASHBOARD = ManagementItem.of({
  label: 'Dashboard',
  route: '',
});

const TOPICS = ManagementItem.of({
  label: 'Themen',
  route: 'topics',
});

const FABRIC_TYPES = ManagementItem.of({
  label: 'Stoffarten',
  route: 'fabric-types',
});

const COLORS = ManagementItem.of({
  label: 'Farben',
  route: 'colors',
});

const FABRICS = ManagementItem.of({
  label: 'Stoffe',
  route: 'fabrics',
});

const ESSENTIAL_MANAGEMENT_ITEMS = [TOPICS, FABRIC_TYPES, COLORS, FABRICS].sort(
  (a, b) => a.label.localeCompare(b.label),
);
const MANAGEMENT_ITEMS = [DASHBOARD, ...ESSENTIAL_MANAGEMENT_ITEMS];

@Component({
  selector: 'app-admin-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeaderComponent implements OnInit, OnDestroy {
  @ViewChild('dropdown')
  protected dropdown!: DropdownComponent;

  private readonly managementItems$: BehaviorSubject<ManagementItem[]> =
    new BehaviorSubject<ManagementItem[]>(MANAGEMENT_ITEMS);
  private readonly selectedManagementItem$: BehaviorSubject<ManagementItem> =
    new BehaviorSubject<ManagementItem>(DASHBOARD);
  private readonly destroy$: Subject<void> = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly adminAuthService: AdminAuthService,
  ) {}

  ngOnInit(): void {
    const route$ = this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map(() => this.router.url),
      startWith(this.router.url),
      map((url) => this.getCurrentRelativeRoute(url)),
    );

    route$.pipe(takeUntil(this.destroy$)).subscribe((route) => {
      Option.someOrNone(
        this.managementItems$.value.find((item) => item.route === route),
      ).ifSome((item) => this.selectedManagementItem$.next(item));
    });
  }

  ngOnDestroy(): void {
    this.managementItems$.complete();
    this.selectedManagementItem$.complete();

    this.destroy$.next();
    this.destroy$.complete();
  }

  updateManagementItem(dropdownItemIds: DropdownItemId[]): void {
    if (dropdownItemIds.length === 0) {
      return;
    }
    if (dropdownItemIds.length !== 1) {
      throw new Error('Expected exactly one management item');
    }

    this.dropdown.toggleOpened();

    const route = dropdownItemIds[0];
    this.router.navigate([`./${route}`], { relativeTo: this.route });
  }

  getManagementItems(): Observable<ManagementItem[]> {
    return this.managementItems$.asObservable();
  }

  getSelectedManagementItem(): Observable<ManagementItem> {
    return this.selectedManagementItem$.asObservable();
  }

  getSelectedManagementItemLabel(): Observable<string> {
    return this.getSelectedManagementItem().pipe(map((item) => item.label));
  }

  getDropdownItems(): Observable<DropdownItem[]> {
    return this.getManagementItems().pipe(
      map((items) =>
        items.map((item) => ({
          id: item.route,
          label: item.label,
        })),
      ),
    );
  }

  logout(): void {
    this.adminAuthService.logout();
    this.router.navigate(['/admin/login']);
  }

  private getCurrentRelativeRoute(url: string): string {
    const relativeUrl = url.substring('/admin'.length);
    let split = relativeUrl.split('/');
    if (split.length >= 2) {
      return split[1];
    }

    return '';
  }
}
