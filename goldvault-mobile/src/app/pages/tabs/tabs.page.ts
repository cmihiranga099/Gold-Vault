import { Component, OnInit, signal } from '@angular/core';
import {
  IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel, IonBadge, IonRouterOutlet
} from '@ionic/angular/standalone';
import { AuthService } from '../../core/auth/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { addIcons } from 'ionicons';
import { homeOutline, notificationsOutline, personOutline, diamondOutline } from 'ionicons/icons';

@Component({
  selector: 'app-tabs',
  standalone: true,
  imports: [IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel, IonBadge, IonRouterOutlet],
  templateUrl: './tabs.page.html',
  styleUrl: './tabs.page.scss'
})
export class TabsPage implements OnInit {
  unreadCount = signal(0);
  private pollHandle?: ReturnType<typeof setInterval>;

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService
  ) {
    addIcons({ homeOutline, notificationsOutline, personOutline, diamondOutline });
  }

  ngOnInit(): void {
    this.loadUnreadCount();
    // Light polling so the badge stays fresh while the app is open —
    // mirrors the web app's topbar bell behavior.
    this.pollHandle = setInterval(() => this.loadUnreadCount(), 60000);
  }

  ngOnDestroy(): void {
    if (this.pollHandle) clearInterval(this.pollHandle);
  }

  private loadUnreadCount(): void {
    const customerId = this.authService.currentUser()?.customerId;
    if (!customerId) return;

    this.notificationService.getCustomerFeed(customerId).subscribe({
      next: (feed) => this.unreadCount.set(feed.unreadCount),
      error: () => {}
    });
  }
}