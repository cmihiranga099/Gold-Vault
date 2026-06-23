export interface ShopDashboardResponse {
    totalCustomers: number;
    activeTickets: number;
    expiringSoonCount: number;
    expiredTickets: number;
    totalOutstandingLoans: number;
    todayCollection: number;
    todayPaymentCount: number;
  }