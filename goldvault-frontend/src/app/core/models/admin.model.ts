export type ShopStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED';

export interface ShopResponse {
  id: number;
  name: string;
  regNumber: string;
  ownerName: string;
  phone: string;
  email: string;
  address: string;
  status: ShopStatus;
  createdAt: string;
}

export interface AdminUserResponse {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: string;
  shopId: number | null;
  enabled: boolean;
  createdAt: string;
}

export interface DashboardSummaryResponse {
  totalShops: number;
  pendingShopApprovals: number;
  activeShops: number;
  totalCustomers: number;
  activeTickets: number;
  expiredTickets: number;
  redeemedTickets: number;
  totalOutstandingLoans: number;
  todayCollection: number;
}

export interface ShopRevenueBreakdown {
  shopId: number;
  shopName: string;
  paymentVolume: number;
  commissionOwed: number;
  paymentCount: number;
}

export interface RevenueReportResponse {
  startDate: string;
  endDate: string;
  totalPaymentVolume: number;
  totalCommission: number;
  totalPaymentCount: number;
  activeShopCount: number;
  activeTicketCount: number;
  byShop: ShopRevenueBreakdown[];
}