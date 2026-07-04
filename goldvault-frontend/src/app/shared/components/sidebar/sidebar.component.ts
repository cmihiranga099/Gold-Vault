import { Component, Input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PanelMenuModule } from 'primeng/panelmenu';
import { ButtonModule } from 'primeng/button';
import { MenuItem } from 'primeng/api';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, PanelMenuModule, ButtonModule, TranslatePipe],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  @Input({ required: true }) items: MenuItem[] = [];

  allExpanded = signal(false);

  toggleAll(): void {
    const next = !this.allExpanded();
    this.allExpanded.set(next);
    this.items.forEach(item => this.setExpandedRecursive(item, next));
    // new array reference so p-panelMenu picks up the mutated `expanded` flags
    this.items = [...this.items];
  }

  private setExpandedRecursive(item: MenuItem, expanded: boolean): void {
    item.expanded = expanded;
    item.items?.forEach(child => this.setExpandedRecursive(child, expanded));
  }
}