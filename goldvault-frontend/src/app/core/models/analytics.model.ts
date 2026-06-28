export interface TopCustomer {
    name:            string;
    ticketCount:     number;
    totalLoanAmount: number;
  }
  
  export interface ShopAnalyticsResponse {
    // KPIs
    activeTickets:        number;
    expiredTickets:       number;
    redeemedTickets:      number;
    totalTicketsEver:     number;
    totalCustomers:       number;
    totalActiveLoanAmount: number;
  
    // Month comparison
    thisMonthCollection:       number;
    lastMonthCollection:       number;
    thisMonthVsLastMonthPct:   number;
  
    // Rates
    redemptionRatePct: number;
    npaRatePct:        number;
  
    // Charts — key = "YYYY-MM"
    ticketsGrantedPerMonth: Record<string, number>;
    collectionPerMonth:     Record<string, number>;
  
    // Payment breakdown
    thisMonthInterest:    number;
    thisMonthPartial:     number;
    thisMonthRedemptions: number;
    thisMonthRenewals:    number;
  
    // Expiry alerts
    expiringSoon7Days:  number;
    expiringSoon30Days: number;
  
    // Top customers
    topCustomers: TopCustomer[];
  }