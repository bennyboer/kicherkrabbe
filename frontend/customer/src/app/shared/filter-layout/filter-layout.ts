import {
	ChangeDetectionStrategy,
	Component,
	EventEmitter,
	Input,
	Output,
	TemplateRef,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { Button } from "primeng/button";
import { Drawer } from "primeng/drawer";
import { Select } from "primeng/select";
import { Tooltip } from "primeng/tooltip";

interface SortOption {
	label: string;
	value: string;
}

@Component({
	selector: "app-filter-layout",
	templateUrl: "./filter-layout.html",
	styleUrl: "./filter-layout.scss",
	standalone: true,
	imports: [CommonModule, FormsModule, Button, Drawer, Select, Tooltip],
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FilterLayout {
	@Input()
	filtersTemplate: TemplateRef<unknown> | null = null;

	@Input()
	sortOptions: SortOption[] = [];

	@Input()
	selectedSort: string = "asc";

	@Input()
	hasActiveFilters: boolean = false;

	@Output()
	sortChange = new EventEmitter<string>();

	@Output()
	resetFilters = new EventEmitter<void>();

	sidebarVisible: boolean = false;

	onSortChange(value: string): void {
		this.sortChange.emit(value);
	}

	openSidebar(): void {
		this.sidebarVisible = true;
	}

	closeSidebar(): void {
		this.sidebarVisible = false;
	}
}
