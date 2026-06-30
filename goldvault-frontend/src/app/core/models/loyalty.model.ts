export interface LoyaltyTransactionResponse {
    id:           number;
    points:       number;
    reason:       string;
    description:  string | null;
    ticketNumber: string | null;
    createdAt:    string;
  }
  
  export interface LoyaltySummaryResponse {
    currentPoints:       number;
    pointValuePercent:   number;
    pointsToNextReward:  number;
    history:             LoyaltyTransactionResponse[];
  }
  
  export interface RedeemPointsRequest {
    customerId: number;
    points:     number;
  }