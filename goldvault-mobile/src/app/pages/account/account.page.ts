import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonIcon
} from '@ionic/angular/standalone';
import { addIcons } from 'ionicons';
import { personCircleOutline, logOutOutline, callOutline, mailOutline } from 'ionicons/icons';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [
    CommonModule,
    IonHeader, IonToolbar, IonTitle, IonContent, IonList, IonItem, IonLabel, IonIcon
  ],
  templateUrl: './account.page.html',
  styleUrl: './account.page.scss'
})
export class AccountPage {
  constructor(public authService: AuthService) {
    addIcons({ personCircleOutline, logOutOutline, callOutline, mailOutline });
  }

  logout(): void {
    this.authService.logout();
  }
}