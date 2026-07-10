import { Component, EventEmitter, OnDestroy, OnInit, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { PopoverModule, Popover } from 'primeng/popover';
import { TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../core/auth/auth.service';
import { LanguageService, AppLang } from '../../../core/services/language.service';
import { ThemeService } from '../../../core/services/theme.service';
import { NotificationService } from '../../../core/services/notification.service';
import { NotificationItemResponse } from '../../../core/models/notification.model';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, ButtonModule, TooltipModule, PopoverModule],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss'
})
export class TopbarComponent implements OnInit, OnDestroy {
  @Output() menuToggle = new EventEmitter<void>();

  private readonly langOrder: AppLang[] = ['en', 'si', 'ta'];
  private pollHandle: ReturnType<typeof setInterval> | undefined;

  notifications = signal<NotificationItemResponse[]>([]);
  unreadCount = signal<number>(0);
  notificationsLoading = signal<boolean>(false);

  constructor(
    public authService: AuthService,
    public themeService: ThemeService,
    public langService: LanguageService,
    private translate: TranslateService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (this.hasNotificationBell()) {
      this.loadNotifications();
      this.pollHandle = setInterval(() => this.loadNotifications(), 60000);
    }
  }

  ngOnDestroy(): void {
    if (this.pollHandle) {
      clearInterval(this.pollHandle);
    }
  }

  hasNotificationBell(): boolean {
    return this.authService.hasRole('ROLE_CUSTOMER', 'ROLE_ADMIN');
  }

  loadNotifications(): void {
    const user = this.authService.currentUser();
    if (!user) return;

    this.notificationsLoading.set(true);
    const feed$ = user.role === 'ROLE_ADMIN'
      ? this.notificationService.getAdminFeed()
      : this.notificationService.getCustomerFeed(user.customerId!);

    feed$.subscribe({
      next: (feed) => {
        this.notifications.set(feed.items);
        this.unreadCount.set(feed.unreadCount);
        this.notificationsLoading.set(false);
      },
      error: () => this.notificationsLoading.set(false)
    });
  }

  onBellClick(event: Event, panel: Popover): void {
    panel.toggle(event);
    this.loadNotifications();
  }

  onNotificationClick(item: NotificationItemResponse, panel: Popover): void {
    const user = this.authService.currentUser();

    // Reminder-table-backed items (ticket-expiry alerts, payment confirmations)
    // are individually markable — AML flags follow the existing review
    // workflow, and promotions have no per-customer read state.
    const markable = item.type === 'DUE_REMINDER' || item.type === 'PAYMENT_CONFIRM';
    if (markable && !item.read && user?.customerId) {
      const notificationId = Number(item.id.replace('reminder-', ''));
      this.notificationService.markReminderRead(user.customerId, notificationId).subscribe(() => {
        this.notifications.update(items =>
          items.map(i => i.id === item.id ? { ...i, read: true } : i));
        this.unreadCount.update(c => Math.max(0, c - 1));
      });
    }

    panel.hide();
    if (item.link) {
      this.router.navigateByUrl(item.link);
    }
  }

  markAllRead(event: Event): void {
    event.stopPropagation();
    const user = this.authService.currentUser();
    if (!user || user.role !== 'ROLE_CUSTOMER' || !user.customerId) return;

    this.notificationService.markAllCustomerRead(user.customerId).subscribe(() => {
      this.notifications.update(items =>
        items.map(i => (i.type === 'DUE_REMINDER' || i.type === 'PAYMENT_CONFIRM') ? { ...i, read: true } : i));
      this.unreadCount.update(() =>
        this.notifications().filter(i => !i.read).length);
    });
  }

  canMarkAllRead(): boolean {
    return this.authService.currentUser()?.role === 'ROLE_CUSTOMER';
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

  iconFor(type: string): string {
    switch (type) {
      case 'DUE_REMINDER': return 'pi-clock';
      case 'PAYMENT_CONFIRM': return 'pi-check-circle';
      case 'AML_ALERT': return 'pi-shield';
      case 'PROMOTION': return 'pi-megaphone';
      default: return 'pi-bell';
    }
  }

  cycleLang(): void {
    const idx = this.langOrder.indexOf(this.langService.currentLang());
    const next = this.langOrder[(idx + 1) % this.langOrder.length];
    this.langService.setLang(next);
  }

  logout(): void {
    this.authService.logout();
  }

  langTooltip(): string {
    return this.translate.instant('topbar.switchLanguage');
  }

  themeTooltip(): string {
    return this.translate.instant(this.themeService.isDark() ? 'topbar.lightMode' : 'topbar.darkMode');
  }

  accountTooltip(): string {
    return this.authService.currentUser()?.fullName ?? this.translate.instant('topbar.account');
  }

  logoutTooltip(): string {
    return this.translate.instant('sidebar.signOut');
  }

  notificationsTooltip(): string {
    return this.translate.instant('topbar.notifications');
  }
}