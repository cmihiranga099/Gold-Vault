export type NotificationItemType = 'DUE_REMINDER' | 'AML_ALERT' | 'PROMOTION';

export interface NotificationItemResponse {
  id:        string;
  type:      NotificationItemType;
  title:     string;
  message:   string;
  link:      string | null;
  createdAt: string;
  read:      boolean;
}

export interface NotificationFeedResponse {
  items:       NotificationItemResponse[];
  unreadCount: number;
}