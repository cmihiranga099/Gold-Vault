import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {
  IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonContent,
  IonRefresher, IonRefresherContent, IonList, IonItem, IonLabel, IonIcon, IonSpinner
} from '@ionic/angular/standalone';
import { RefresherCustomEvent } from '@ionic/angular';
import { addIcons } from 'ionicons';
import {
  timeOutline, checkmarkCircleOutline, shieldOutline, megaphoneOutline,
  notificationsOffOutline
} from 'ionicons/icons';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { NotificationItemResponse } from '../../core/models/notification.model';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [
    CommonModule,
    IonHeader, IonToolbar, IonTitle, IonButtons, IonButton, IonContent,
    IonRefresher, IonRefresherContent, IonList, IonItem, IonLabel, IonIcon, IonSpinner
  ],
  templateUrl: './notifications.page.html',
  styleUrl: './notifications.page.scss'
})
export class NotificationsPage implements OnInit {
  items = signal<NotificationItemResponse[]>([]);
  loading = signal(true);
  unreadCount = signal(0);

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    addIcons({ timeOutline, checkmarkCircleOutline, shieldOutline, megaphoneOutline, notificationsOffOutline });
  }

  ngOnInit(): void {
    this.load();
  }

  load(event?: RefresherCustomEvent): void {
    const customerId = this.authService.currentUser()?.customerId;
    if (!customerId) { this.loading.set(false); event?.target.complete(); return; }

    if (!event) this.loading.set(true);
    this.notificationService.getCustomerFeed(customerId).subscribe({
      next: (feed) => {
        this.items.set(feed.items);
        this.unreadCount.set(feed.unreadCount);
        this.loading.set(false);
        event?.target.complete();
      },
      error: () => {
        this.loading.set(false);
        event?.target.complete();
      }
    });
  }

  markAllRead(): void {
    const customerId = this.authService.currentUser()?.customerId;
    if (!customerId) return;

    this.notificationService.markAllCustomerRead(customerId).subscribe(() => {
      this.items.update(items =>
        items.map(i => (i.type === 'DUE_REMINDER' || i.type === 'PAYMENT_CONFIRM') ? { ...i, read: true } : i));
      this.unreadCount.update(() => this.items().filter(i => !i.read).length);
    });
  }

  onItemClick(item: NotificationItemResponse): void {
    const customerId = this.authService.currentUser()?.customerId;
    const markable = item.type === 'DUE_REMINDER' || item.type === 'PAYMENT_CONFIRM';

    if (markable && !item.read && customerId) {
      const notificationId = Number(item.id.replace('reminder-', ''));
      this.notificationService.markReminderRead(customerId, notificationId).subscribe(() => {
        this.items.update(items => items.map(i => i.id === item.id ? { ...i, read: true } : i));
        this.unreadCount.update(c => Math.max(0, c - 1));
      });
    }

    // The backend's `link` field points at web-app routes (e.g. "/customer/dashboard"),
    // which don't exist in this app's route structure — remap by type instead.
    // (AML_ALERT never appears here; that type is admin-only.)
    if (item.type === 'DUE_REMINDER' || item.type === 'PAYMENT_CONFIRM') {
      this.router.navigateByUrl('/tabs/dashboard');
    }
  }

  iconFor(type: string): string {
    switch (type) {
      case 'DUE_REMINDER': return 'time-outline';
      case 'PAYMENT_CONFIRM': return 'checkmark-circle-outline';
      case 'AML_ALERT': return 'shield-outline';
      case 'PROMOTION': return 'megaphone-outline';
      default: return 'time-outline';
    }
  }

  timeAgo(iso: string): string {
    const diffMs = Date.now() - new Date(iso).getTime();
    const mins = Math.floor(diffMs / 60000);
    if (mins < 1) return 'just now';
    if (mins < 60) return `${mins}m ago`;
    const hours = Math.floor(mins / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    if (days < 7) return `${days}d ago`;
    return new Date(iso).toLocaleDateString();
  }
}