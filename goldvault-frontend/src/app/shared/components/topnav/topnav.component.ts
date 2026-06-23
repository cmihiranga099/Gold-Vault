import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-topnav',
  standalone: true,
  imports: [CommonModule, ButtonModule, TooltipModule],
  templateUrl: './topnav.component.html',
  styleUrl: './topnav.component.scss'
})
export class TopnavComponent {
  constructor(public authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}